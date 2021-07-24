package org.ardenus.engine.graphics.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.ardenus.engine.graphics.GraphicsException;

/**
 * A shader for an OpenGL program.
 * 
 * @see Program
 */
public class Shader {

	protected static String getShaderTypeName(int glShaderType) {
		switch (glShaderType) {
			case GL_VERTEX_SHADER:
				return "GL_VERTEX_SHADER";
			case GL_FRAGMENT_SHADER:
				return "GL_FRAGMENT_SHADER";
			case GL_GEOMETRY_SHADER:
				return "GL_GEOMETRY_SHADER";
			case GL_TESS_CONTROL_SHADER:
				return "GL_TESS_CONTROL_SHADER";
			case GL_TESS_EVALUATION_SHADER:
				return "GL_TESS_EVALUATION_SHADER";
			default:
				return null;
		}
	}

	private static int requireShaderType(int glShaderType) {
		switch (glShaderType) {
			case GL_VERTEX_SHADER:
			case GL_FRAGMENT_SHADER:
			case GL_GEOMETRY_SHADER:
			case GL_TESS_CONTROL_SHADER:
			case GL_TESS_EVALUATION_SHADER:
				return glShaderType;
			default:
				throw new IllegalArgumentException("not an OpenGL shader type");
		}
	}

	protected final int glShaderType;
	protected final int h_glShader;

	private boolean hasSource;
	private boolean compiled;
	protected Program program;
	private boolean destroyed;

	/**
	 * Constructs a new {@code Shader} and generates an OpenGL shader to
	 * manipulate and send instructions to.
	 * 
	 * @param glShaderType
	 *            the OpenGL shader type.
	 * @throws IllegalArgumentException
	 *             if {@code glShaderType} is not an OpenGL shader type.
	 * @throws GraphicsException
	 *             if the shader could not be created.
	 */
	public Shader(int glShaderType) {
		this.glShaderType = requireShaderType(glShaderType);
		this.h_glShader = glCreateShader(glShaderType);
		if (h_glShader == GL_NONE) {
			throw new GraphicsException("failed to create shader");
		}
	}

	protected void requireIntact() {
		if (destroyed == true) {
			throw new IllegalStateException("shader deleted");
		}
	}

	protected void requireHasSource() {
		if (hasSource == false) {
			throw new IllegalStateException("no source for shader");
		}
	}

	protected void requireUncompiled() {
		if (compiled == true) {
			throw new IllegalStateException("shader already compiled");
		}
	}

	protected void requireCompiled() {
		if (compiled == false) {
			throw new IllegalStateException("shader not compiled");
		}
	}

	protected void requireNotAttached() {
		if (program != null) {
			throw new IllegalStateException(
					"shader already attached to program");
		}
	}

	/**
	 * Updates the shader source.
	 * <p>
	 * Once the source code for a shader has been set, it must be compiled via
	 * the {@link #compile()} method.
	 * 
	 * @param src
	 *            the shader source code.
	 * @return this shader.
	 * @throws NullPointerException
	 *             if {@code src} is {@code null}.
	 * @throws IllegalStateException
	 *             if the shader is closed or has already been compiled.
	 */
	public Shader setSource(String src) {
		Objects.requireNonNull(src, "src");
		this.requireIntact();
		this.requireUncompiled();
		glShaderSource(h_glShader, src);
		this.hasSource = true;
		return this;
	}

	/**
	 * Updates the shader source.
	 * <p>
	 * Once the source code for a shader has been set, it must be compiled via
	 * the {@link #compile()} method.
	 * <p>
	 * This function is a shorthand {@link #setSource(String)}, with the
	 * contents of {@code in} being read into a string and passed as the value
	 * for {@code src}.
	 * 
	 * @param in
	 *            the stream whose contents to use as the shader source.
	 * @return this shader.
	 * @throws NullPointerException
	 *             if {@code in} is {@code null}.
	 * @throws IllegalStateException
	 *             if the shader is closed or has already been compiled.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public Shader setSource(InputStream in) throws IOException {
		Objects.requireNonNull(in, "in");
		String src = IOUtils.toString(in, (Charset) null);
		return this.setSource(src);
	}
	
	/**
	 * Updates the shader source.
	 * <p>
	 * Once the source code for a shader has been set, it must be compiled via
	 * the {@link #compile()} method.
	 * <p>
	 * <p>
	 * This function is a shorthand {@link #setSource(InputStream)}, with the
	 * contents of {@code url} being opened as a stream and passed as the value
	 * for {@code in}.
	 * 
	 * @param url
	 *            the URL whose contents to use as the shader source.
	 * @return this shader.
	 * @throws NullPointerException
	 *             if {@code url} is {@code null}.
	 * @throws IllegalStateException
	 *             if the shader is closed or has already been compiled.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public Shader setSource(URL url) throws IOException {
		Objects.requireNonNull(url, "url");
		InputStream in = url.openStream();
		return this.setSource(in);
	}

	/**
	 * Updates the shader source.
	 * <p>
	 * Once the source code for a shader has been set, it must be compiled via
	 * the {@link #compile()} method.
	 * <p>
	 * This function is a shorthand {@link #setSource(InputStream)}, with the
	 * contents of {@code file} being wrapped into a {@link FileInputStream} and
	 * passed as the value for {@code in}.
	 * 
	 * @param file
	 *            the file whose contents to use as the shader source.
	 * @return this shader.
	 * @throws NullPointerException
	 *             if {@code file} is {@code null}.
	 * @throws IllegalStateException
	 *             if the shader is closed or has already been compiled.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public Shader setSource(File file) throws IOException {
		Objects.requireNonNull(file, "file");
		return this.setSource(new FileInputStream(file));
	}

	/**
	 * Compiles the shader.
	 * <p>
	 * Once the shader has been compiled, it must be attached to a program via
	 * the {@link Program#attach(Shader)} method. After the program has been
	 * linked, this shader will be automatically closed. This will prevent the
	 * shader from being used with any other programs.
	 * 
	 * @return this shader.
	 * @throws IllegalStateException
	 *             if the shader is closed, has no source code, or has already
	 *             been compiled.
	 * @throws GraphicsException
	 *             if shader compilation fails.
	 */
	public Shader compile() {
		this.requireIntact();
		this.requireHasSource();
		this.requireUncompiled();

		glCompileShader(h_glShader);
		if (glGetShaderi(h_glShader, GL_COMPILE_STATUS) != GL_TRUE) {
			String shaderLog = glGetShaderInfoLog(h_glShader);
			throw new GraphicsException(shaderLog);
		}
		this.compiled = true;
		return this;
	}

	/**
	 * Deletes the shader from OpenGL.
	 * <p>
	 * Usually, a method like this would be an implementation of Java's
	 * {@code Closeable} interface. However, this method should only be called
	 * after this shader has been compiled and attached to a linked OpenGL
	 * program. Implementing the interface would prevent us from having this
	 * marked as {@code protected} so only {@link Program} could call it.
	 */
	protected void destroy() {
		if (destroyed == true) {
			return;
		}
		glDeleteShader(h_glShader);
		this.destroyed = true;
	}

}
