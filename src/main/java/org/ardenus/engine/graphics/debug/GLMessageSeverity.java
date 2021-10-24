package org.ardenus.engine.graphics.debug;

import static org.lwjgl.opengl.GL43.*;

import org.apache.logging.log4j.Level;

public enum GLMessageSeverity {

	UNKNOWN(0, "Unknown", 0, Level.INFO),

	NOTIFICATION(GL_DEBUG_SEVERITY_NOTIFICATION, "Notification", 1, Level.INFO),
	LOW(GL_DEBUG_SEVERITY_LOW, "Low", 2, Level.WARN),
	MEDIUM(GL_DEBUG_SEVERITY_MEDIUM, "Medium", 3, Level.ERROR),
	HIGH(GL_DEBUG_SEVERITY_HIGH, "High", 4, Level.ERROR);

	public final int severity;
	public final String name;
	public final int level;
	public final Level logLevel;

	private GLMessageSeverity(int severity, String name, int level,
			Level logLevel) {
		this.severity = severity;
		this.name = name;
		this.level = level;
		this.logLevel = logLevel;
	}

	public static GLMessageSeverity bySeverity(int severity) {
		for (GLMessageSeverity debugSeverity : GLMessageSeverity.values()) {
			if (debugSeverity.severity == severity) {
				return debugSeverity;
			}
		}
		return null;
	}

}
