package org.ardenus.engine.audio;

/**
 * Signals that an error relating to the audio system has occurred.
 */
public class AudioException extends RuntimeException {

	private static final long serialVersionUID = 3995081614142098979L;

	/**
	 * Constructs a new {@code AudioException} with the specified detail message
	 * and cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link Throwable#getCause()} method). A {@code null} value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.
	 */
	public AudioException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code AudioException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 */
	public AudioException(String message) {
		this(message, (Throwable) null);
	}

	/**
	 * Constructs a new {@code AudioException} with the specified cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link Throwable#getCause()} method). A {@code null} value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.
	 */
	public AudioException(Throwable cause) {
		this((String) null, cause);
	}

	/**
	 * Constructs a new {@code AudioException} with no detail message.
	 */
	public AudioException() {
		this((String) null, (Throwable) null);
	}

}
