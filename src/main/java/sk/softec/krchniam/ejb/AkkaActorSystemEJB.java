package sk.softec.krchniam.ejb;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AkkaActorSystemEJB {

	private static final Logger LOGGER = LoggerFactory.getLogger(AkkaActorSystemEJB.class);

	private ActorSystem actorSystem;

	@PostConstruct
	public void init() {
		actorSystem = ActorSystem.create("akka-integration");
		// shutdown uses scala.concurrent.ExecutionContext.Implicits.global, adding hook for shutting down
		//
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
}
