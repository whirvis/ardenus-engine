package org.ardenus.engine;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.whirvex.cmd.Option;
import com.whirvex.cmd.args.Args;

/**
 * The Ardenus game engine.
 * <p>
 * TODO: Go into depth what this class is for.
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

	private static boolean started;
	private static boolean devmode;

	private Ardenus() {
		// Static class
	}

	/**
	 * Returns whether or not the engine is running in development mode.
	 * <p>
	 * TODO: Define what development mode being enabled means.
	 * <p>
	 * Development mode is enabled by specifying the <code>devmode</code>
	 * option at startup via the JVM program arguments.
	 * 
	 * @return <code>true</code> if the engine is running in development mode,
	 *         <code>false</code> otherwise.
	 */
	public static boolean devmode() {
		return devmode;
	}

	/**
	 * Starts the Ardenus Engine.
	 * <p>
	 * TODO: Go into depth what this method does.
	 * 
	 * @param args
	 *            the engine boot arguments.
	 * @throws IllegalStateException
	 *             if the engine has already been started.
	 */
	public static synchronized void start(Args args) {
		if (started == true) {
			throw new IllegalStateException("engine already started");
		}
		devmode = args.has("devmode");
		started = true;
	}

	/**
	 * Java program entry point.
	 * 
	 * @param args
	 *            the program arguments.
	 */
	public static void main(String[] args) {
		Logger logger = LogManager.getLogger("main");
		
		// Instantiate startup options
		logger.info("Instantiating startup options");
		Option help = Option.opt().key('h', "help").value(false)
				.desc("Displays engine info and start options").build();
		Option devmode = Option.opt().key("devmode").value(false)
				.desc("If the engine should run in development mode").build();
		
		// Start engine
		logger.info("Starting engine");
		start(Args.parse(args, help, devmode));
	}

}
