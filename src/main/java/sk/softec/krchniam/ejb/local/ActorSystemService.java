package sk.softec.krchniam.ejb.local;

public interface ActorSystemService {
	<T> T ask(String actorName, Object message) throws WrappedException;

	void tell(String actorName, Object message);
}
