package org.ardenus.engine.state;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ardenus.engine.Game;

/**
 * A state within a {@link Game} ran by the Ardenus Engine.
 */
public abstract class GameState {

	public final String id;
	public final Game game;
	private final Set<Entity> entities;
	private boolean initialized;

	private boolean entered;

	/**
	 * Constructs a new {@code GameState}.
	 * 
	 * @param id
	 *            the state ID.
	 * @param game
	 *            the game this state belongs to.
	 */
	public GameState(String id, Game game) {
		this.id = Objects.requireNonNull(id, "id");
		this.game = Objects.requireNonNull(game, "game");
		this.entities = new HashSet<>();
	}

	/**
	 * Initializes the game state.
	 * <p>
	 * If the state has already been initialized, this method will be a no-op.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void init() throws Exception {
		if (initialized) {
			return;
		}
		this.onInit();
		this.initialized = true;
	}

	/**
	 * Called when the game state is initialized.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void onInit() throws Exception {
		/* optional implement */
	}

	/**
	 * Adds an entity to the game state.
	 * <p>
	 * This entity will be updated and rendered automatically.
	 * 
	 * @param entity
	 *            the entity to add.
	 * @throws NullPointerException
	 *             if {@code entity} is {@code null}.
	 */
	protected void addEntity(Entity entity) {
		Objects.requireNonNull(entity, "entity");
		entity.setup(this);
		entities.add(entity);
	}

	/**
	 * Removes an entity from the game state.
	 * 
	 * @param entity
	 *            the entity to remove.
	 */
	protected void removeEntity(Entity entity) {
		if (entity != null && entities.contains(entity)) {
			entities.remove(entity);
			entity.kill(this);
		}
	}

	/**
	 * Enters the game state.
	 * <p>
	 * If the state has already been entered, this method will be a no-op.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void enter() throws Exception {
		if (entered) {
			return;
		}
		for (Entity entity : entities) {
			entity.setup(this);
		}
		this.onEnter();
		this.entered = true;
	}

	/**
	 * Called when the game state is entered.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void onEnter() throws Exception {
		/* optional implement */
	}

	/**
	 * Updates the game state.
	 * 
	 * @param delta
	 *            the update delta.
	 * @throws IllegalStateException
	 *             if the state is not currently entered.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void update(long delta) throws Exception {
		if (!entered) {
			throw new IllegalStateException("not entered");
		}
		this.onUpdate(delta);
		for (Entity entity : entities) {
			entity.update(this, delta);
		}
	}

	/**
	 * Called when the game state is updated.
	 * 
	 * @param delta
	 *            the update delta.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public abstract void onUpdate(long delta) throws Exception;

	/**
	 * Renders the game state.
	 * 
	 * @throws IllegalStateException
	 *             if the state is not currently entered.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void render() throws Exception {
		if (!entered) {
			throw new IllegalStateException("not entered");
		}
		for (Entity entity : entities) {
			entity.render(this);
		}
		this.onRender();
	}

	/**
	 * Called when the game state is rendered.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public abstract void onRender() throws Exception;

	/**
	 * Leaves the game state.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void leave() throws Exception {
		for(Entity entity : entities) {
			entity.sleep(this);
		}
		this.onLeave();
	}

	/**
	 * Called when the game state is left.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void onLeave() throws Exception {
		/* optional implement */
	}

}
