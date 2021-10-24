package org.ardenus.engine.graphics.debug;

import static org.lwjgl.opengl.GL43.*;

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

	OTHER(GL_DEBUG_TYPE_OTHER, "Other");

	public final int type;
	public final String name;

	private GLMessageType(int type, String name) {
		this.type = type;
		this.name = name;
	}

	public static GLMessageType byType(int type) {
		for (GLMessageType debugType : GLMessageType.values()) {
			if (debugType.type == type) {
				return debugType;
			}
		}
		return null;
	}

}
