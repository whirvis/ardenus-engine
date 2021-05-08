package org.ardenus.engine.audio.vorbis;

import java.io.IOException;
import java.lang.reflect.Field;

import org.lwjgl.stb.STBVorbis;

public class VorbisException extends IOException {

	private static final long serialVersionUID = -4312057543953771468L;

	private static String getErrorName(int errorCode) {
		for (Field field : STBVorbis.class.getFields()) {
			if (field.getType() != int.class) {
				continue;
			}

			try {
				int fieldValue = field.getInt(null);
				if (fieldValue == errorCode) {
					return field.getName();
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		throw new IllegalArgumentException("unknown error " + errorCode);
	}

	public VorbisException(int errorCode) {
		super(getErrorName(errorCode));
	}

}
