package org.ardenus.engine.state;

public abstract class Entity {

	public abstract void setup(GameState state);
	
	public abstract void sleep(GameState state);

	public abstract void kill(GameState state);

	public abstract void update(GameState state, long delta);

	public void render(GameState state) {
		/* optional implement */
	}

}
