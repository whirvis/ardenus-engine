package org.ardenus.engine.util;

import static org.lwjgl.opengl.GL11.*;

/**
 * Contains utility methods for working with handles. Typically, these handles
 * come from lower level APIs (OpenGL, OpenAL, etc.) Functions relating to
 * handles will be added to this class as they are needed.
 */
public final class Handles {
	
	private Handles() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks that the specified OpenGL handle is not {@code GL_NONE}. This
	 * method is designed primarily for doing handle validation where OpenGL
	 * objects are generated, as demonstrated below:
	 * 
	 * <pre>
	 * 
	 * public static void init() {
	 * 	h_glIndices = Handles.requireGL(glGenBuffers());
	 * }
	 * </pre>
	 * 
	 * @param handle
	 *            the OpenGL handle to check for noneness.
	 * @return {@code handle} if not {@code GL_NONE}.
	 * @throws NoHandleException
	 *             if {@code handle} is {@code GL_NONE}.
	 */
	public static int requireGL(int handle) {
		if (handle == GL_NONE) {
			throw new NoHandleException();
		}
		return handle;
	}

	/**
	 * Checks that the specified OpenGL handle is not {@code GL_NONE}. This
	 * method is designed primarily for doing handle validation where OpenGL
	 * objects are generated, as demonstrated below:
	 * 
	 * <pre>
	 * 
	 * public static void init(int h_glInices) {
	 * 	Handles.requireGL(h_glIndices, "VBO cannot be GL_NONE");
	 * }
	 * </pre>
	 * 
	 * @param handle
	 *            the OpenGL handle to check for noneness.
	 * @param message
	 *            detail message to be used in the event that a
	 *            {@code NoneHandlException} is thrown.
	 * @return {@code handle} if not {@code GL_NONE}.
	 * @throws NoHandleException
	 *             if {@code handle} is {@code GL_NONE}.
	 */
	public static int requireGL(int handle, String message) {
		if (handle == GL_NONE) {
			throw new NoHandleException(message);
		}
		return handle;
	}

}
