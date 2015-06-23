package sk.softec.krchniam.ejb;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.softec.krchniam.ejb.local.ActorSystemService;
import sk.softec.krchniam.ejb.local.WrappedException;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.impl.ExecutionContextImpl;

@Singleton
@Startup
@Local(ActorSystemService.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AkkaActorSystemEJB implements ActorSystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AkkaActorSystemEJB.class);

	private ActorSystem actorSystem;

	private FiniteDuration askDurationTimeout;

	@Resource(lookup = "iszi/akka-remote-system")
	private String akkaRemote;

	@Resource(lookup = "iszi/akka-ask-timeout")
	private Integer askDurationTimeoutInteger;

	@PostConstruct
	public void init() {
		askDurationTimeout = FiniteDuration.create(askDurationTimeoutInteger, TimeUnit.SECONDS);
		actorSystem = ActorSystem.create("akka-integration");
		// shutdown uses scala.concurrent.ExecutionContext.Implicits.global, adding hook for shutting down
//		actorSystem.registerOnTermination(new Runnable() {
//			@Override
//			public void run() {
//				ExecutionContextImpl executorImpl = (ExecutionContextImpl) ExecutionContext.Implicits$.MODULE$.global();
//				AbstractExecutorService executorService = (AbstractExecutorService) executorImpl.executor();
//
//				executorService.shutdown();
//				try {
//					executorService.awaitTermination(10, TimeUnit.SECONDS);
//					LOGGER.info("Shutting down ExecutionContext.Implicits.global completed.");
//				} catch (InterruptedException e) {
//					LOGGER.error("Shutting down ExecutionContext.Implicits.global failed.", e);
//				}
//			}
//		});

		LOGGER.info("Creating actor system {}", actorSystem);
	}

	@PreDestroy
	public void destroy() {
		actorSystem.shutdown();
		actorSystem.awaitTermination();
		LOGGER.info("ActorSystem terminated.");
	}

	@Override
	public <T> T ask(String actorName, Object message) throws WrappedException {
		String path = createRemotePath(actorName);
		try {
			return askSynchronous(message, path);
		} catch (Exception e) {
			throw new WrappedException( String.format("Calling of remote actor on address: %s failed", path), e);
		}
	}

	private String createRemotePath(String actorName) {
		String path = String.format("%s/user/%s", akkaRemote, actorName);
		LOGGER.debug("remotePath created: {}", path);
		return path;
	}

	@SuppressWarnings("unchecked")
	private <T> T askSynchronous(Object message, String path) throws Exception {
		return (T)Await.result(askActor(path, message), askDurationTimeout);
	}

	private Future<Object> askActor(String path, Object message) {
		ActorSelection actorSelection = actorSystem.actorSelection(path);
		return Patterns.ask(actorSelection, message, askDurationTimeout.length());
	}

	@Override
	public void tell(String actorName, Object message) {
		String path = createRemotePath(actorName);
		ActorSelection actorSelection = actorSystem.actorSelection(path);
		actorSelection.tell(message, null);
	}
}
