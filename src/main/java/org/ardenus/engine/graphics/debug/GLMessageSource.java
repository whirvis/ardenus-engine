package org.ardenus.engine.graphics.debug;

import static org.lwjgl.opengl.GL43.*;

public enum GLMessageSource {

	UNKNOWN(0, "Unknown"),

	API(GL_DEBUG_SOURCE_API, "API"),
	WINDOW_SYSTEM(GL_DEBUG_SOURCE_WINDOW_SYSTEM, "Window System"),
	SHADER_COMPILER(GL_DEBUG_SOURCE_SHADER_COMPILER, "Shader Compiler"),
	THIRD_PARTY(GL_DEBUG_SOURCE_THIRD_PARTY, "Third Party"),
	APPLICATION(GL_DEBUG_SOURCE_APPLICATION, "Application"),

	OTHER(GL_DEBUG_SOURCE_OTHER, "Other");

	public final int source;
	public final String name;

	private GLMessageSource(int source, String name) {
		this.source = source;
		this.name = name;
	}

	public static GLMessageSource bySource(int source) {
		for (GLMessageSource debugSource : GLMessageSource.values()) {
			if (debugSource.source == source) {
				return debugSource;
			}
		}
		return null;
	}

}
