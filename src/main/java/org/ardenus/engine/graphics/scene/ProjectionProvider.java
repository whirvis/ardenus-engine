package org.ardenus.engine.graphics.scene;

import org.joml.Matrix4f;

/**
 * A class which provides an projection matrix as a source of data.
 */
public interface ProjectionProvider {

	/**
	 * Returns the projection matrix.
	 * <p>
	 * This value will likely be static, but it is very possible that it could
	 * change over time. Do not cache this value unless absolutely certain or
	 * value changes can be accounted for when they occur.
	 * 
	 * @return the projection matrix.
	 */
	public Matrix4f getProjectionMatrix();

}
