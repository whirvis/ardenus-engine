package org.ardenus.engine.graphics;

import java.util.Objects;

import org.ardenus.engine.graphics.provider.ViewProvider;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a camera, which is used to travel around the game world and what
 * is currently being rendered.
 * 
 * @see Viewport
 */
public class Camera implements ViewProvider {

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

	@Override
	public Matrix4f getViewMatrix() {
		viewMatrix.setTranslation(-pos.x, -pos.y, -pos.z);
		return this.viewMatrix;
	}

}
