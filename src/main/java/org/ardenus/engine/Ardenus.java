package org.ardenus.engine;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.audio.Audio;

import com.whirvex.args.Args;
import com.whirvex.args.ArgsParser;
import com.whirvex.args.Option;

/**
 * The Ardenus game engine.
 * <p>
 * TODO: Go into depth what this class is for.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public final class Ardenus {

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
	private static Game game;
	private static long startTime;

	private Ardenus() {
		// Static class
	}

	/**
	 * Returns if the engine is running in development mode.
	 * <p>
	 * TODO: Define what development mode being enabled means.
	 * <p>
	 * Development mode is enabled by specifying the <code>devmode</code> option
	 * at startup via the JVM program arguments.
	 * 
	 * @return <code>true</code> if the engine is running in development mode,
	 *         <code>false</code> otherwise.
	 */
	public static boolean devmode() {
		return devmode;
	}

	/**
	 * Returns the game being run by the engine.
	 * 
	 * @return the game.
	 */
	public static Game game() {
		return game;
	}

	/**
	 * Returns the time the engine started as according to
	 * {@link System#currentTimeMillis()}.
	 * 
	 * @return the time the engine started.
	 */
	public static long startTime() {
		return startTime;
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
		devmode = args.hasFlag("--devmode");

		if (args.hasFlag("--game")) {
			try {
				File gameFile = new File(args.getFlag("--game", 0));
				game = Game.load(gameFile);
			} catch (IOException e) {
				// TODO
			}
		} else if (game == null) {
			args.require.hasFlag("--game");
		}
		
		startTime = System.currentTimeMillis();
		
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
		
		
		Option help = new Option("help", "Displays engine info and start options", false, "-h", "--help");
		Option devmode = new Option("devmode", "If the engine should run in development mode", false, "--devmode");
		Option game = new Option("game", "The path to the game JAR", true, "-g", "--game");
		
		// Start engine
		logger.info("Starting engine");
		start(ArgsParser.parse(args, help, devmode, game));
	}

	/**
	 * Java program entry point where the game being run can be specified. This
	 * is meant purely for testing
	 * 
	 * @param game
	 * the game.
	 * @param args
	 * the arguments.
	 */
	public static void main(Game game, String[] args) {
		Ardenus.game = game;
		main(args);
	}

}
