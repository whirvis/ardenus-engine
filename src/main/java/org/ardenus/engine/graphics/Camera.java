package org.ardenus.engine.graphics;

import java.util.Objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a camera, which is used to travel around the game world and what
 * is currently being rendered.
 * 
 * @see Viewport
 */
public class Camera {

	public final Viewport viewport;
	public final Vector3f pos;
	private final Matrix4f view;

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
		this.pos = new Vector3f();
		this.view = new Matrix4f();
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
	 * Returns the camera view matrix.
	 * 
	 * @return the camera view matrix.
	 */
	public Matrix4f getViewMatrix() {
		view.setTranslation(-pos.x, -pos.y, -pos.z);
		return this.view;
	}

}
