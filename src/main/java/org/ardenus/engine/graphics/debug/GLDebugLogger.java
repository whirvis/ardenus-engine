package org.ardenus.engine.graphics.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.graphics.Graphics;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

/**
 * Logs debug messages sent from the OpenGL API to the console.
 */
public class GLDebugLogger extends GLDebugMessageCallback {

	private final Logger logger;

	public GLDebugLogger() {
		this.logger = LogManager.getLogger("OpenGL");
	}

	@Override
	public void invoke(int sourceId, int typeId, int id, int severityId,
			int length, long message, long userParam) {
		GLMessageSeverity severity = GLMessageSeverity.bySeverity(severityId);
		if (severity.level < Graphics.getDebugLevel().level) {
			return;
		}

		GLMessageSource source = GLMessageSource.bySource(sourceId);
		GLMessageType type = GLMessageType.byType(typeId);
		logger.log(severity.logLevel, "(" + type.name + ") " + source.name
				+ " says " + MemoryUtil.memASCII(message));
	}

}
