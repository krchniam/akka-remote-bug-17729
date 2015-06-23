package sk.softec.krchniam.ejb.local;

/**
 * Obalovacia vynimka. Dedi z Exception a teda je vyhodenie z EJB-cka nesposobi 
 * - rollback transakcie 
 * - zrusenie instancie EJB-cka.
 */
public class WrappedException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrappedException(String message, Throwable cause) {
		super(message, cause);
	}
}
