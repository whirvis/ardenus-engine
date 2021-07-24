package org.ardenus.engine.graphics;

import org.ardenus.engine.graphics.provider.OrthoProvider;
import org.ardenus.engine.graphics.provider.ProjectionProvider;
import org.joml.Matrix4f;

/**
 * Represents a render viewport, which is used to contain the contents of a
 * rendered scene at a pre-defined position and scale.
 * 
 * @see Camera
 */
public class Viewport implements OrthoProvider, ProjectionProvider {

	public final float x, y;
	public final float width, height;
	public final float aspectRatio, fov;
	public final float zNear, zFar;
	public final Matrix4f ortho, projection;

	/**
	 * Creates a viewport.
	 * 
	 * @param x
	 *            the viewport X-axis position to the left.
	 * @param y
	 *            the viewport Y-axis position to the top.
	 * @param width
	 *            the viewport width.
	 * @param height
	 *            the viewport height.
	 * @param fov
	 *            the field of view, specified in degrees.
	 * @param zNear
	 *            the closest something can be on the Z-axis before it
	 *            disappears from of the render viewport.
	 * @param zFar
	 *            the farthest something can be on the Z-axis before it
	 *            disappears from of the render viewport.
	 * @throws IllegalArgumentException
	 *             if {@code width} or {@code height} are negative or
	 *             {@code zNear} is not less than {@code zFar}.
	 */
	public Viewport(float x, float y, float width, float height, float fov,
			float zNear, float zFar) {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("width < 0 || height < 0");
		} else if (zNear >= zFar) {
			throw new IllegalArgumentException("zNear >= zFar");
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.aspectRatio = width / height;
		this.fov = fov;
		this.zNear = zNear;
		this.zFar = zFar;

		this.ortho =
				new Matrix4f().ortho(x, x + width, y + height, y, zNear, zFar);
		this.projection = new Matrix4f().perspective(
				(float) Math.toRadians(fov), aspectRatio, zNear, zFar);
	}

	@Override
	public Matrix4f getOrthoMatrix() {
		return this.ortho;
	}

	@Override
	public Matrix4f getProjectionMatrix() {
		return this.projection;
	}

}
