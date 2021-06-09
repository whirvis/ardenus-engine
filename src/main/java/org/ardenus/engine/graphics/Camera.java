package org.ardenus.engine.graphics;

import java.util.Objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a camera, which is used to travel around the game world and what
 * is currently being rendered.
 * 
 * @see Window
 * @see Viewport
 */
public class Camera {

	public final Viewport viewport;
	public final Vector3f pos;
	private Matrix4f viewMatrix;

	/**
	 * Creates a camera.
	 * 
	 * @param viewport
	 *            the viewport this camera is bound to.
	 * @throws NullPointerException
	 *             if {@code viewport} is {@code null}.
	 */
	public Camera(Viewport viewport) {
		this.viewport = Objects.requireNonNull(viewport, "viewport");
		this.pos = new Vector3f(0.0F, 0.0F, 0.0F);
		this.viewMatrix = new Matrix4f();
	}

	/**
	 * Returns the viewport the camera is bound to.
	 * 
	 * @return the viewport the camera is bound to.
	 */
	public Viewport getViewport() {
		return this.viewport;
	}

	/**
	 * Returns the generated view model matrix. This can be used in shaders to
	 * apply the proper effect of having an in-game camera.
	 * 
	 * @return the view model matrix.
	 */
	public Matrix4f getViewMatrix() {
		viewMatrix.setTranslation(-pos.x, -pos.y, -pos.z);
		return this.viewMatrix;
	}

}
