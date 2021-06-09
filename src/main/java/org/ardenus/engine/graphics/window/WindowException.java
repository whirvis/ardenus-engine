package org.ardenus.engine.graphics.window;

import org.ardenus.engine.graphics.GraphicsException;

/**
 * Signals that an error relating to a window has occurred.
 */
public class WindowException extends GraphicsException {

	private static final long serialVersionUID = 1507967068617119351L;
	
	/**
	 * Constructs a new {@code WindowException} with the specified detail message
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
	public WindowException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code WindowException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 */
	public WindowException(String message) {
		this(message, (Throwable) null);
	}

	/**
	 * Constructs a new {@code WindowException} with the specified cause.
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
	public WindowException(Throwable cause) {
		this((String) null, cause);
	}

	/**
	 * Constructs a new {@code WindowException} with no detail message.
	 */
	public WindowException() {
		this((String) null, (Throwable) null);
	}

}
