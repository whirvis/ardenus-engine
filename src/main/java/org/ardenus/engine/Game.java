package org.ardenus.engine;

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

}
