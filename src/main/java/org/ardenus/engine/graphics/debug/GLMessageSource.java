package org.ardenus.engine.graphics.debug;

import static org.lwjgl.opengl.GL43.*;

/**
 * The source of a debug message from OpenGL.
 */
public enum GLMessageSource {

	UNKNOWN(0, "Unknown"),

	API(GL_DEBUG_SOURCE_API, "API"),
	WINDOW_SYSTEM(GL_DEBUG_SOURCE_WINDOW_SYSTEM, "Window System"),
	SHADER_COMPILER(GL_DEBUG_SOURCE_SHADER_COMPILER, "Shader Compiler"),
	THIRD_PARTY(GL_DEBUG_SOURCE_THIRD_PARTY, "Third Party"),
	APPLICATION(GL_DEBUG_SOURCE_APPLICATION, "Application"),

	/**
	 * A source that is not from the {@link #API}, {@link #WINDOW_SYSTEM},
	 * {@link #SHADER_COMPILER}, {@link #THIRD_PARTY}, or {@link #APPLICATION}.
	 */
	OTHER(GL_DEBUG_SOURCE_OTHER, "Other");

	public final int source;
	public final String name;

	private GLMessageSource(int source, String name) {
		this.source = source;
		this.name = name;
	}

	/**
	 * Returns a {@code GLMessageSource} by its OpenGL source ID.
	 * 
	 * @param source
	 *            the OpenGL source ID.
	 * @return the {@code GLMessageSource} with an OpenGL source ID identical to
	 *         {@code source}, {@code null} if none exists.
	 */
	public static GLMessageSource bySource(int source) {
		for (GLMessageSource debugSource : GLMessageSource.values()) {
			if (debugSource.source == source) {
				return debugSource;
			}
		}
		return null;
	}

}
