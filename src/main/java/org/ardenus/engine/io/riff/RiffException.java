package org.ardenus.engine.io.riff;

import java.io.IOException;

/**
 * Signals that an error relating to a {@code RIFF} container has occurred.
 * 
 * @see RiffFile
 * @see RiffInputStream
 */
public class RiffException extends IOException {

	private static final long serialVersionUID = -473688291701040037L;

	/**
	 * Constructs a {@code RiffException} with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A {@code null} value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public RiffException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a {@code RiffException} with the specified detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public RiffException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@code RiffException} with the specified cause and a detail
	 * message of {@code (cause == null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}). This
	 * constructor is useful for RIFF exceptions that are little more than
	 * wrappers for other throwables.
	 *
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A {@code null} value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public RiffException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a {@code RiffException} with {@code null} as its error detail
	 * message.
	 */
	public RiffException() {
		super();
	}

}
