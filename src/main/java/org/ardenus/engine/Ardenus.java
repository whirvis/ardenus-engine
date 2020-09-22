package org.ardenus.engine;

import java.io.File;

/**
 * The Ardenus game engine.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public class Ardenus {

	public static final String ENGINE_NAME = "Ardenus Engine";
	public static final String ENGINE_VERSION = "v0.0.1 ALPHA";
	public static final long ENGINE_VERSION_ID = 0x0000000000000001L;
	public static final String ENGINE_VERSION_CODENAME = "Jumpscare";
	
	/**
	 * The folder in which the Ardenus Engine stores things such as
	 * configuration files.
	 */
	public static final File ENGINE_ROOT = new File("./ardenus/");
	
	private Ardenus() {
		// Static class
	}
	
}
