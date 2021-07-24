package org.ardenus.engine.graphics.provider;

import org.joml.Matrix4f;

/**
 * A class which provides an orthographic matrix as a source of data.
 * <p>
 * Orthographic matrices are used to bind what is rendered on the screen to a
 * certain set of coordinates. That being, what is the left most, the right
 * most, the top most, and the bottom most. The nearest and farthest objects are
 * also taken into account for orthographic matrices. This allows developers to
 * specify their own units in how graphics should be drawn to the screen.
 */
public interface OrthoProvider {

	/**
	 * Returns the orthograhic matrix.
	 * <p>
	 * This value will likely be static, but it is very possible that it could
	 * change over time. Do not cache this value unless absolutely certain or
	 * value changes can be accounted for when they occur.
	 * 
	 * @return the orthographic matrix.
	 */
	public Matrix4f getOrthoMatrix();

}
