package org.ardenus.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A game running through the Ardenus Engine.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public abstract class Game {

	/**
	 * The folder in which game dependencies are stored.
	 */
	public static final File LIB_FOLDER = new File("./lib/");

	/**
	 * Creates a <code>ClassLoader</code> for a game JAR file.
	 * <p>
	 * TODO: Go more into more depth of what this method does.
	 * 
	 * @param gameFile
	 *            the JAR file of the game.
	 * @return the instantiated class loader, of which game files can be grabbed
	 *         from.
	 */
	private static ClassLoader getClassLoader(File gameFile) {
		try {
			ArrayList<URL> libJars = new ArrayList<URL>();
			libJars.add(gameFile.toURI().toURL());
			if (LIB_FOLDER.exists()) {
				for (File file : LIB_FOLDER.listFiles()) {
					if (file.getName().endsWith(".jar")) {
						libJars.add(file.toURI().toURL());
					}
				}
			} else {
				LIB_FOLDER.mkdirs();
			}
			return new URLClassLoader(libJars.toArray(new URL[libJars.size()]),
					Game.class.getClassLoader());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a game from a specified file.
	 * 
	 * @param gameFile
	 *            the JAR file of the game.
	 * @return the instantiated <code>Game</code> instance.
	 * @throws IOException
	 *             if <code>gameFile</code> does not exist or some other I/O
	 *             error occurs.
	 */
	public static Game load(File gameFile) throws IOException {
		Objects.requireNonNull(gameFile, "gameFile cannot be null");
		if (!gameFile.exists()) {
			throw new FileNotFoundException("gameFile does not exist");
		}

		try {
			// Load game class
			ClassLoader classLoader = getClassLoader(gameFile);
			ArdenusManifest manifest = new ArdenusManifest(classLoader);
			Class<? extends Game> gameClazz = manifest.getMain(classLoader);

			// Instantiate game instance
			Game game = gameClazz.newInstance();
			game.onLoad();
			return game;
		} catch (ClassNotFoundException e) {
			throw new IOException("main class not found", e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IOException("failed to instantiate main class", e);
		}
	}

	private Logger logger;
	private boolean running;

	/**
	 * The games title. By default, this is what the window title is set to.
	 */
	public final String title;

	/**
	 * Constructs a new <code>Game</code>.
	 * 
	 * @param title
	 *            the game title.
	 * @throws NullPointerException
	 *             if <code>title</code> is <code>null</code>.
	 */
	protected Game(String title) {
		this.logger = LogManager.getLogger("game");
		this.title = Objects.requireNonNull(title, "title cannot be null");
	}

	public synchronized final void start() {
		if (running == true) {
			throw new IllegalStateException("game already running");
		}

		this.running = true;
	}

	public synchronized final void stop() {
		if (running == false) {
			throw new IllegalStateException("game not running");
		}

		this.running = false;
	}

	/**
	 * Called when the game has been loaded. This is right after the class has
	 * been instantiated in the {@link #load(File)} method.
	 */
	public void onLoad() {
	}

}
