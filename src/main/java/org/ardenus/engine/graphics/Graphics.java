package org.ardenus.engine.graphics;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.*;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.graphics.debug.GLDebugLogger;
import org.ardenus.engine.graphics.debug.GLMessageSeverity;
import org.ardenus.engine.graphics.window.Window;

/**
 * The graphic system for the Ardenus Engine.
 * 
 * @see #init()
 */
public final class Graphics {

	private static final Logger LOG = LogManager.getLogger(Graphics.class);
	private static final int[] CAPABILITY_IDS = new int[] {
			GL_DEBUG_OUTPUT, GL_TEXTURE_2D, GL_BLEND
	};

	private static boolean initialized;
	private static GLMessageSeverity debugLevel;

	/**
	 * @return the debug logging level, may be {@code null}.
	 */
	public static GLMessageSeverity getDebugLevel() {
		return debugLevel;
	}

	/**
	 * @param severity
	 *            the minimum severity level that a debug message must be in
	 *            order for it to be logged.
	 * @throws NullPointerException
	 *             if {@code severity} is {@code null}.
	 */
	public static void setDebugLevel(GLMessageSeverity severity) {
		debugLevel = Objects.requireNonNull(severity, "severity");
		LOG.info("Set debug level to " + severity.name());
	}

	/**
	 * A GLFW context must be made on the calling thread before the graphics
	 * system can be initialized. The window whose context is made current will
	 * have all graphics drawn to it. At the moment, only one window is
	 * supported for graphics. Support for multiple instances of the graphics
	 * system across different threads (to enable drawing to multiple windows)
	 * is planned to be added sometime in the future.
	 * 
	 * @see Window#makeContextCurrent()
	 */
	public static void init() {
		if (initialized == true) {
			LOG.error("Already initialized");
			return;
		}

		LOG.info("Creating capabilities...");
		createCapabilities();
		for (int capabilityId : CAPABILITY_IDS) {
			glEnable(capabilityId);
			LOG.debug("Enabled capability with ID " + capabilityId);
		}
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);

		LOG.info("Registering debug logger...");
		if (debugLevel == null) {
			setDebugLevel(GLMessageSeverity.LOW);
		}
		glDebugMessageCallback(new GLDebugLogger(), 0L);

		initialized = true;
		LOG.info("Initialized system");
	}

}
