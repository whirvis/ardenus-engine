package org.ardenus.engine;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.graphics.Graphics;
import org.ardenus.engine.graphics.window.Window;
import org.ardenus.engine.input.Input;

import com.whirvex.args.Args;
import com.whirvex.args.ArgsParser;
import com.whirvex.args.Option;
import com.whirvex.event.EventManager;

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

	private static long startTime;
	private static long lastUpdate;
	private static EventManager events;

	private static Window window;
	private static Game game;

	private Ardenus() {
		throw new UnsupportedOperationException();
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
	
	public static EventManager getEvents() {
		return events;
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
	 * Returns the window being used by the engine.
	 * 
	 * @return the window.
	 */
	public static Window window() {
		return window;
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

	private static void update() {
		Window.pollEvents();
		Input.poll();

		long delta = 1;
		long currentTime = System.currentTimeMillis();
		if (lastUpdate != 0) {
			delta = currentTime - lastUpdate;
		}

		game.update(delta);
		if (window.shouldClose()) {
			game.stop();
		}

		lastUpdate = currentTime;
	}

	private static void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		game.render();
		window.swapBuffers();
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
			// File gameFile = new File(args.getFlag("--game", 0));
			// TODO: game = Game.load(gameFile);
		} else if (game == null) {
			args.require.hasFlag("--game");
		}

		events = new EventManager();
		events.register(game);
		startTime = System.currentTimeMillis();

		Window.init();
		window = new Window(1024, 768, game.getTitle());
		window.makeContextCurrent();
		Graphics.init();

		Audio.init(events);
		Input.init(events);

		started = true;

		game.start();
		while (game.isRunning()) {
			update();
			render();
		}

		Input.terminate();
		Audio.terminate();

		window.close();
		Window.terminate();
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

		/* @formatter: off */
		Option help = new Option("help",
				"Displays engine info and start options",
				false, "-h", "--help");
		Option devmode = new Option("devmode",
				"If the engine should run in dev mode",
				false, "--devmode");
		Option game = new Option("game",
				"The path to the game JAR",
				true, "-g", "--game");
		/* @formatter: on */

		// Start engine
		logger.info("Starting engine");
		start(ArgsParser.parse(args, help, devmode, game));
	}

	/**
	 * Java program entry point where the game being run can be specified. This
	 * is meant purely for testing
	 * 
	 * @param game
	 *            the game.
	 * @param args
	 *            the arguments.
	 */
	public static void main(Game game, String[] args) {
		Ardenus.game = game;
		main(args);
	}

}
