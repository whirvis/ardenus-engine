package org.ardenus.engine.io;

/**
 * A resource which can be loaded before usage.
 * 
 * @param <T>
 *            the resource type.
 */
public abstract class Resource<T> {

	/**
	 * Loads resource data outside of the main thread if possible.
	 * <p>
	 * This is to allow for smoother load screens as well as faster load times.
	 * If data must be loaded on the main thread (such as texture binding,
	 * buffer data piping, etc.), {@link #load()} can be used instead.
	 * <p>
	 * This method will always be called first before {@link #load()}.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void offload() throws Exception {
		/* optional implement */
	}

	/**
	 * Loads resource data inside of the main thread.
	 * <p>
	 * This is always called inside of the main thread to ensure that contextual
	 * APIs like OpenGL and OpenAL can legally be called upon to finish loading
	 * operations.
	 * <p>
	 * This method will always be called after {@link #offload()}.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void load() throws Exception {
		/* optional implement */
	}

	/**
	 * @return the resource after having been loaded by {@link #offload()}
	 *         and/or {@link #load()}, may be {@code null}.
	 */
	public abstract T getLoaded();

}
