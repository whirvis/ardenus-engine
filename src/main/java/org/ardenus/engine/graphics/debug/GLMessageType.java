package org.ardenus.engine.graphics.debug;

import static org.lwjgl.opengl.GL43.*;

/**
 * The type of an OpenGL debug message.
 */
public enum GLMessageType {

	UNKNOWN(0, "Unknown"),

	ERROR(GL_DEBUG_TYPE_ERROR, "Error"),
	DEPRECATED_BEHAVIOR(GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR,
			"Deprecated Behavior"),
	UNDEFINED_BEHAVIOR(GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR, "Undefined Behavior"),
	PORTABILITY(GL_DEBUG_TYPE_PORTABILITY, "Portability"),
	PERFORMANCE(GL_DEBUG_TYPE_PERFORMANCE, "Performance"),
	MARKER(GL_DEBUG_TYPE_MARKER, "Marker"),
	PUSH_GROUP(GL_DEBUG_TYPE_PUSH_GROUP, "Push Group"),
	POP_GROUP(GL_DEBUG_TYPE_POP_GROUP, "Pop Group"),

	/**
	 * A message that does not fit within the category of {@link #ERROR},
	 * {@link #DEPRECATED_BEHAVIOR}, {@link #UNDEFINED_BEHAVIOR},
	 * {@link #PORTABILITY}, {@link #PERFORMANCE}, {@link #MARKER},
	 * {@link #PUSH_GROUP}, or {@link #POP_GROUP}.
	 */
	OTHER(GL_DEBUG_TYPE_OTHER, "Other");

	public final int type;
	public final String name;

	private GLMessageType(int type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * Returns a {@code GLMessageType} by its OpenGL type ID.
	 * 
	 * @param type
	 *            the OpenGL type ID.
	 * @return the {@code GLMessageType} with an OpenGL type ID identical to
	 *         {@code type}, {@code null} if none exists.
	 */
	public static GLMessageType byType(int type) {
		for (GLMessageType debugType : GLMessageType.values()) {
			if (debugType.type == type) {
				return debugType;
			}
		}
		return null;
	}

}
