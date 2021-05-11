package org.ardenus.engine.audio.vorbis;

import java.io.IOException;
import java.lang.reflect.Field;

import org.lwjgl.stb.STBVorbis;

/**
 * Signals that an error relating to an {@link VorbisFile OGG} Vorbis file has
 * occured.
 */
public class VorbisException extends IOException {

	private static final long serialVersionUID = -4312057543953771468L;

	private static String stbVorbisErrorName(int code) {
		for (Field field : STBVorbis.class.getFields()) {
			if (field.getType() != int.class) {
				continue;
			}

			try {
				int fieldValue = field.getInt(null);
				if (fieldValue == code) {
					return field.getName();
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return "unknown STB Vorbis error";
	}

	private final int errorCode;

	/**
	 * Constructs a new {@code VorbisException} with the specified detail
	 * message and cause.
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
	public VorbisException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = -1;
	}

	/**
	 * Constructs a new {@code VorbisException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 */
	public VorbisException(String message) {
		this(message, (Throwable) null);
	}

	/**
	 * Constructs a new {@code VorbisException} with the specified cause.
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
	public VorbisException(Throwable cause) {
		this((String) null, cause);
	}

	/**
	 * Constructs a new {@code VorbisException} with the detail message being
	 * the name of the specified STB Vorbis error code.
	 * 
	 * @param errorCode
	 *            the STB Vorbis error code.
	 */
	public VorbisException(int errorCode) {
		super(stbVorbisErrorName(errorCode));
		this.errorCode = errorCode;
	}

	/**
	 * Constructs a new {@code VorbisException} with no detail message.
	 */
	public VorbisException() {
		this((String) null, (Throwable) null);
	}

	/**
	 * Returns the STB Vorbis error code.
	 * 
	 * @return the error code, {@code -1} if none was specified during
	 *         construction.
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

}
