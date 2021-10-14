package org.ardenus.engine.state;

public abstract class Entity {

	public abstract void setup(GameState state) throws Exception;
	
	public abstract void sleep(GameState state) throws Exception;

	public abstract void kill(GameState state) throws Exception;

	public abstract void update(GameState state, long delta) throws Exception;

	public void render(GameState state) throws Exception {
		/* optional implement */
	}

}
