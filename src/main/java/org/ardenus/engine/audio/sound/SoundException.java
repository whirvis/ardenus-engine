package org.ardenus.engine.audio.sound;

import org.ardenus.engine.audio.AudioException;

/**
 * Signals that an error relating to a {@link Sound} has occurred.
 */
public class SoundException extends AudioException {

	private static final long serialVersionUID = 7257431667017413666L;

	/**
	 * Constructs a new {@code SoundException} with the specified detail message
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
	public SoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code SoundException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 */
	public SoundException(String message) {
		this(message, (Throwable) null);
	}

	/**
	 * Constructs a new {@code SoundException} with the specified cause.
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
	public SoundException(Throwable cause) {
		this((String) null, cause);
	}

	/**
	 * Constructs a new {@code SoundException} with no detail message.
	 */
	public SoundException() {
		this((String) null, (Throwable) null);
	}

}
