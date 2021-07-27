package org.ardenus.engine.graphics.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.ardenus.engine.graphics.GraphicsException;
import org.ardenus.engine.util.BeResponsible;
import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

/**
 * An OpenGL program made up of shaders.
 * 
 * @see Shader
 */
public class Program implements Closeable {

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, int value) {
		glUniform1i(location, value);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, float value) {
		glUniform1f(location, value);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, boolean value) {
		glUniform1i(location, value ? 0x01 : 0x00);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Vector2f value) {
		glUniform2f(location, value.x, value.y);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Vector3f value) {
		glUniform3f(location, value.x, value.y, value.z);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Vector4f value) {
		glUniform4f(location, value.x, value.y, value.z, value.w);
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Matrix2f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(8);
			value.get(buffer);
			glUniformMatrix2fv(location, false, buffer);
		}
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Matrix3f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(12);
			value.get(buffer);
			glUniformMatrix3fv(location, false, buffer);
		}
	}

	/**
	 * Sets a uniform for the currently installed program.
	 * <p>
	 * This is a cheap operation at the cost of parameter checks.<br>
	 * If an invalid uniform location is specified, tough!
	 * <p>
	 * Also note the value of a uniform is retained (even between program
	 * switches) once it is set. Use this knowledge to reduce the uniform
	 * updates for programs, as their cheap cost can build up.
	 * 
	 * @param location
	 *            the uniform location.
	 * @param value
	 *            the new uniform value.
	 * @see #getUniformLoc(String)
	 * @see #use()
	 */
	@BeResponsible
	public static void setUniform(int location, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(16);
			value.get(buffer);
			glUniformMatrix4fv(location, false, buffer);
		}
	}

	private final int h_glProgram;
	private final Map<Integer, Shader> attached;

	private boolean linked;
	private boolean closed;

	/**
	 * Constructs a new {@code Program} and generates an OpenGL program to
	 * manipulate and send instructions to.
	 * 
	 * @throws GraphicsException
	 *             if the program could not be created.
	 */
	public Program() {
		this.h_glProgram = glCreateProgram();
		if (h_glProgram == GL_NONE) {
			throw new GraphicsException("failed to create program");
		}
		this.attached = new HashMap<>();
	}

	private void requireAttached(int... glShaderTypes) {
		for (int glShaderType : glShaderTypes) {
			if (!attached.containsKey(glShaderType)) {
				throw new IllegalStateException("missing "
						+ Shader.getShaderTypeName(glShaderType) + " shader");
			}
		}
	}

	private void requireUnlinked() {
		if (linked == true) {
			throw new IllegalStateException("program already linked");
		}
	}

	private void requireLinked() {
		if (linked == false) {
			throw new IllegalStateException("program not linked");
		}
	}

	/**
	 * Attaches a shader to the program.
	 * <p>
	 * Once a shader has been attached to a program, it cannot be detached.
	 * After all desired shaders have been linked to the program, the program
	 * must be linked via {@link #link()}.
	 * 
	 * @return this program.
	 * @param shader
	 *            the shader to attach.
	 * @throws NullPointerException
	 *             if {@code shader} is {@code null}.
	 * @throws IllegalStateException
	 *             if this program is already linked; if {@code shader} is not
	 *             compiled or already attached to another shader.
	 */
	public Program attach(Shader shader) {
		Objects.requireNonNull(shader, "shader");
		if (shader.program == this) {
			return this;
		}

		this.requireUnlinked();
		shader.requireCompiled();
		shader.requireNotAttached();

		glAttachShader(h_glProgram, shader.h_glShader);
		attached.put(shader.glShaderType, shader);
		shader.program = this;
		return this;
	}

	/**
	 * Attaches the given shaders to the program.
	 * <p>
	 * Once a shader has been attached to a program, it cannot be detached.
	 * After all desired shaders have been linked to the program, the program
	 * must be linked via {@link #link()}.
	 * 
	 * @param shaders
	 *            the shaders to attach.
	 * @return this program.
	 * @throws NullPointerException
	 *             if {@code shaders} or one of its values are {@code null}.
	 * @throws IllegalStateException
	 *             if this program is already linked; if a given shader is not
	 *             compiled or already attached to another shader.
	 */
	public Program attach(Shader... shaders) {
		Objects.requireNonNull(shaders, "shaders");
		for (Shader shader : shaders) {
			this.attach(shader);
		}
		return this;
	}

	/**
	 * Links the program to OpenGL.
	 * <p>
	 * After linking is successfully completed, all previously attached shaders
	 * will be deleted. Once a program has been linked, it must be installed
	 * into OpenGL for rendering via {@link #use()}.
	 * 
	 * @throws IllegalStateException
	 *             if the program is already linked; if there is no attached
	 *             {@code GL_VERTEX_SHADER}.
	 * @throws GraphicsException
	 *             if the program fails to link.
	 */
	public void link() {
		this.requireUnlinked();
		this.requireAttached(GL_VERTEX_SHADER);

		glLinkProgram(h_glProgram);
		if (glGetProgrami(h_glProgram, GL_LINK_STATUS) != GL_TRUE) {
			String programLog = glGetProgramInfoLog(h_glProgram);
			throw new GraphicsException(programLog);
		}

		/*
		 * Once a program has been linked in OpenGL, the shaders that are
		 * attached to it are no longer needed. OpenGL shaders can technically
		 * be used with other programs after this. However, to my knowledge,
		 * this is a rare occasion. I've personally never seen it done, or even
		 * heard of. So, I've decided just detach and close the shaders here.
		 */
		Iterator<Shader> attachedI = attached.values().iterator();
		while (attachedI.hasNext()) {
			Shader shader = attachedI.next();
			glDetachShader(h_glProgram, shader.h_glShader);
			shader.destroy();
			attachedI.remove();
		}

		this.linked = true;
	}

	/**
	 * Queries OpenGL for the location of a uniform based on its name.
	 * <p>
	 * This is required for safe code that will consistently update the correct
	 * uniform. However, this is a costly operation. It is recommended to cache
	 * the values returned from this method into a variable somewhere for later.
	 * A clean and easy way to do this would be to make a class extending
	 * {@code Program}, override {@link #link()}, and fetch the uniforms there:
	 * 
	 * <pre>
	 * public class BubblesProgram extends Program {
	 *     
	 *     private int u_bubbleX;
	 *     private int u_bubblyY;
	 *     private int u_bubbleColor;
	 *     
	 *     &commat;Override
	 *     public void link() {
	 *         super.link();
	 *         
	 *         this.u_bubbleX = this.getUniformLocation("bubble_x");
	 *         this.u_bubbleY = this.getUniformLocation("bubble_y");
	 *         this.u_bubbleColor = this.getUniformLocation("bubble_color");
	 *     }
	 *     
	 * }
	 * </pre>
	 * 
	 * @param name
	 *            the uniform name.
	 * @return the uniform location.
	 * @throws NullPointerException
	 *             if {@code name} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if no such uniform {@code name} exists in this program.
	 * @see #setUniform(int, int)
	 */
	public int getUniformLoc(String name) {
		/*
		 * Usually for get methods, I would just have this return something like
		 * -1. However, I've come to think that is a bit silly. From now on, I
		 * will write methods to throw exceptions for any value that doesn't
		 * make sense in that context. So... why would you pass null to this?
		 */
		Objects.requireNonNull(name, "name");
		this.requireLinked();

		int location = glGetUniformLocation(h_glProgram, name);
		if (location < 0) {
			throw new IllegalArgumentException(
					"no such uniform \"" + name + "\"");
		}
		return location;
	}

	/**
	 * Installs the program for use into OpenGL.
	 * <p>
	 * It is possible to use multiple render programs in OpenGL. This is an
	 * expensive operations. As such, it is recommended that shader programs
	 * performing similar operations be merged into what's called an <a href=
	 * "http://hacksoflife.blogspot.com/2009/02/uber-shaders-evolution-or-optimization.html">
	 * Uber Shader</a>. However, this should only be done branching can be
	 * avoided. If branching is used, <a href=
	 * "https://gamedev.stackexchange.com/questions/59476/using-two-shaders-instead-of-one-with-if-statements">the
	 * effects are either negated or worsened</a>.</i>
	 * <p>
	 * An example of branching:
	 * 
	 * <pre>
	 * #define DRAW_RECT 0
	 * #define DRAW_IMG  1
	 * 
	 * uniform int task;
	 * 
	 * void drawRect();
	 * void drawImg();
	 * 
	 * void main(void) {
	 *     /&ast; this destroys performance! &ast;/
	 *     /&ast; no, using a switch won't fix this! &ast;/
	 *     if (task == DRAW_RECT) {
	 *         drawRect();
	 *     } else if (task == DRAW_IMG) {
	 *         drawImg();
	 *     }
	 * }
	 * </pre>
	 * 
	 * @throws IllegalStateException
	 *             if the program is not linked.
	 */
	public void use() {
		this.requireLinked();
		glUseProgram(h_glProgram);
	}

	@Override
	public void close() throws IOException {
		if (this.closed == true) {
			return;
		}
		glDeleteProgram(h_glProgram);
		this.closed = true;
	}

}
