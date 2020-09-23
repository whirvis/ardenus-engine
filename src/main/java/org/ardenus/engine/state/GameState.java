package org.ardenus.engine.state;

import java.util.Objects;

import org.ardenus.engine.Game;

/**
 * A class which acts as a state within the {@link Game} it's initialized for.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public abstract class GameState {

	private boolean initialized;
	private Game game;

	/**
	 * Initializes the state to be used with a game.
	 * 
	 * @param game
	 *            the game this state is being initialized for.
	 * @throws IllegalStateException
	 *             if this state has already been initialized.
	 * @throws NullPointerException
	 *             if <code>game</code> is <code>null</code>.
	 */
	public final void init(Game game) {
		if (initialized == true) {
			throw new IllegalStateException("state already initialized");
		}
		this.game = Objects.requireNonNull(game, "game cannot be null");
		this.init(game);
		this.initialized = true;
	}

	/**
	 * Returns whether or not this state has been initialized.
	 * 
	 * @return <code>true</code> if this state has been initialized,
	 *         <code>false</code> otherwise.
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Returns the game this state is initialized under.
	 * 
	 * @return the game this state is initialized under, <code>null</code> if
	 *         this state has yet to be initialized.
	 */
	public Game getGame() {
		return this.game;
	}

	/**
	 * Initializes the game state after internal initialization through
	 * {@link #init(Game)}. This should not be called by another method.
	 * 
	 * @param game
	 *            the game the state is initialized under.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void onInit(Game game) throws Exception {
	}

	/**
	 * Enters the game state, preparing it for {@link #onUpdate(Game, long)} and
	 * {@link #onRender(Game)} calls.
	 * 
	 * @param game
	 *            the game the state is initialized under.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void onEnter(Game game) throws Exception {
	}

	/**
	 * Updates the game state.
	 * 
	 * @param game
	 *            the game the state is initialized under.
	 * @param delta
	 *            how many milliseconds have passed since the last engine
	 *            update.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public abstract void onUpdate(Game game, long delta) throws Exception;

	/**
	 * Renders the game state.
	 * 
	 * @param game
	 *            the game the state is initialized under.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public abstract void onRender(Game game) throws Exception;

	/**
	 * Leaves the game state.
	 * 
	 * @param game
	 *            the game the state is initialized under.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void onLeave(Game game) throws Exception {
	}

}
