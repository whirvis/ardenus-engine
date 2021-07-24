package org.ardenus.engine.graphics.provider;

import org.joml.Matrix4f;

/**
 * A class which provides a view matrix as a source of data.
 */
public interface ViewProvider {

	/**
	 * Returns the view model matrix.
	 * <p>
	 * This value is almost guaranteed to change over time, do <i>not</i> cache
	 * it this value unless absolute certain. It will likely be used for the
	 * effect of having an in-game camera.
	 * 
	 * @return the view model matrix.
	 */
	public Matrix4f getViewMatrix();

}
