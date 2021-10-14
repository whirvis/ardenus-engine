package org.ardenus.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.state.GameState;

import com.whirvex.event.EventListener;

/**
 * A game running through the Ardenus Engine.
 */
public abstract class Game implements EventListener {

	private final Logger logger;
	private final String title;
	private final Map<String, GameState> states;

	private boolean running;
	private GameState currentState;

	/**
	 * Creates a new {@code Game} instance.
	 * 
	 * @param title
	 *            the game title.
	 * @throws NullPointerException
	 *             if {@code title} is {@code null}.
	 */
	protected Game(String title) {
		this.logger = LogManager.getLogger("game");
		this.title = Objects.requireNonNull(title, "title");
		this.states = new HashMap<>();
	}

	/**
	 * Returns the game's title.
	 * 
	 * @return the game's title.
	 */
	public String getTitle() {
		return this.title;
	}

	public void addState(GameState state) {
		if(running) {
			throw new IllegalStateException();
		}
		Objects.requireNonNull(state, "state");
		if (state.game != this) {
			throw new IllegalArgumentException(
					"state does not belong to this game");
		} else if (states.containsKey(state.id)) {
			logger.warn("state with ID \"" + state.id + "\" overriden");
		}
		states.put(state.id, state);
	}

	public void enterState(String id) {
		Objects.requireNonNull(id, "id");
		GameState state = states.get(id);
		if (state == null) {
			throw new IllegalArgumentException(
					"no such state with ID \"" + id + "\"");
		}

		if (currentState != null) {
			try {
				currentState.leave();
			} catch (Exception e) {
				logger.error("Error leaving game state", e);
			}
		}

		try {
			state.enter();
			this.currentState = state;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized final void start() {
		if (running == true) {
			throw new IllegalStateException("already running");
		}
		try {
			for(GameState state : states.values()) {
				state.init();
			}
			this.onStart();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		logger.info("Started game");
		this.running = true;
	}

	/**
	 * Called when the game is started.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void onStart() throws Exception {
		/* optional implement */
	}

	public boolean isRunning() {
		return this.running;
	}

	public synchronized final void update(long delta) {
		try {
			this.onUpdate(delta);
			if (currentState != null) {
				currentState.update(delta);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void onUpdate(long delta) throws Exception {
		/* optional implement */
	}

	public synchronized final void render() {
		try {
			if (currentState != null) {
				currentState.render();
			}
			this.onRender();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void onRender() throws Exception {
		/* optional implement */
	}

	public synchronized final void stop() {
		if (running == false) {
			throw new IllegalStateException("already stopped");
		}

		this.onStop();
		logger.info("Stopped game");
		this.running = false;
	}

	/**
	 * Called when the game has been stopped.
	 */
	protected void onStop() {
		/* optional implement */
	}

	/**
	 * Called when the game has been loaded. This is right after the class has
	 * been instantiated in the {@link #load(File)} method.
	 */
	protected void onLoad() {
		/* optional implement */
	}

}
