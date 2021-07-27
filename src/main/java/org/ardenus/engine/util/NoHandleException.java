package org.ardenus.engine.util;

/**
 * Signals that a handle for an object is missing.
 */
public class NoHandleException extends RuntimeException {

	private static final long serialVersionUID = -3233691434896111191L;

	/**
	 * Constructs a new {@code NoHandleException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 */
	public NoHandleException(String message) {
		super(message, (Throwable) null);
	}

	/**
	 * Constructs a new {@code NoHandleException} with no detail message.
	 */
	public NoHandleException() {
		super((String) null, (Throwable) null);
	}

}
