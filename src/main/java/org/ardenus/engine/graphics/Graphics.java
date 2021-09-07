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

	private static final Logger LOGGER = LogManager.getLogger(Graphics.class);
	private static final int[] CAPABILITY_IDS =
			new int[] { GL_DEBUG_OUTPUT, GL_TEXTURE_2D, GL_BLEND };

	private static boolean initialized;
	private static GLMessageSeverity debugLevel;

	/**
	 * Returns the debug logging level.
	 * 
	 * @return the debug logging level, may be {@code null} if graphics have not
	 *         been initialized.
	 */
	public static GLMessageSeverity getDebugLevel() {
		return debugLevel;
	}

	/**
	 * Sets the debug logging level.
	 * 
	 * @param severity
	 *            the minimum severity level that a debug message must be in
	 *            order for it to be logged.
	 * @throws NullPointerException
	 *             if {@code severity} is {@code null}.
	 */
	public static void setDebugLevel(GLMessageSeverity severity) {
		debugLevel = Objects.requireNonNull(severity, "severity");
		LOGGER.info("Set debug level to " + severity.name());
	}

	/**
	 * Initializes the graphics system.
	 * <p>
	 * Before this method can be called, a GLFW context must be made on the
	 * calling thread. This can be achieved via {@link Window#init()}.
	 */
	public static void init() {
		if (initialized == true) {
			LOGGER.error("Already initialized");
			return;
		}

		LOGGER.info("Creating capabilities...");
		createCapabilities();
		for (int capabilityId : CAPABILITY_IDS) {
			glEnable(capabilityId);
			LOGGER.debug("Enabled capability with ID " + capabilityId);
		}
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);

		LOGGER.info("Registering debug logger...");
		if (debugLevel == null) {
			setDebugLevel(GLMessageSeverity.LOW);
		}
		glDebugMessageCallback(new GLDebugLogger(), 0L);
		
		initialized = true;
		LOGGER.info("Initialized system");
	}

}
