package org.ardenus.engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.ardenus.engine.graphics.shader.Program;
import org.ardenus.engine.graphics.shader.Uniform;
import org.ardenus.engine.util.BeResponsible;
import org.ardenus.engine.util.Handles;
import org.ardenus.engine.util.NoHandleException;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

/**
 * A drawable image.
 * <p>
 * No image is drawable without an implementation that specicies how its data
 * should be loaded and rendered. Proper usage of implementations is dependent
 * on what image will be drawn to screen.
 */
public class Image implements Closeable {

	@Uniform
	private static int img_scale, img_pos, img_sampler, img_color;

	private static final int IMG_VERT_ID = 0;
	private static final int IMG_UV_ID = 1;

	private static final int[] INDICES = new int[] { 0, 1, 2, 2, 3, 0 };
	private static final float[] UV = new float[] { 0, 1, 0, 0, 1, 0, 1, 1 };

	private static boolean initialized;
	private static int h_glIndices;
	private static int h_glUV;
	private static int imgCount;

	/**
	 * Initializes the image system.
	 * 
	 * @throws GraphicsException
	 *             if the index or UV buffer fail to generate.
	 */
	private static void init() {
		if (initialized) {
			return;
		}

		h_glIndices = Handles.requireGL(glGenBuffers(), "h_glIndices");
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, h_glIndices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

		h_glUV = Handles.requireGL(glGenBuffers(), "h_glUV");
		glBindBuffer(GL_ARRAY_BUFFER, h_glUV);
		glBufferData(GL_ARRAY_BUFFER, UV, GL_STATIC_DRAW);

		initialized = true;
	}

	/**
	 * De-initializes the image system.
	 */
	private static void deinit() {
		if (!initialized) {
			return;
		}
		glDeleteBuffers(h_glIndices);
		glDeleteBuffers(h_glUV);
		initialized = false;
	}

	/**
	 * Allocates a buffer big enough to fit the given {@link BufferedImage} and
	 * reads its pixels into a buffer that is fit for an OpenGL texture.
	 * <p>
	 * This function accounts for the fact that OpenGL UV coordinates are from
	 * bottom to top, rather than top to bottom. To accomodate, the image is
	 * loaded upside down. ARGB pixels are also converted to RGBA.
	 * 
	 * @param img
	 *            the image to load.
	 * @return the texture buffer.
	 * @throws NullPointerException
	 *             if {@code img} is {@code null}.
	 */
	private static ByteBuffer genPixBuf(BufferedImage img) {
		Objects.requireNonNull(img, "img");
		ByteBuffer buffer = BufferUtils.createByteBuffer(
				img.getWidth() * img.getHeight() * Integer.BYTES);

		/**
		 * Because OpenGL is OpenGL, it's UV coordinates start from the bottom.
		 * Since image coordinates start from the top, this results in them
		 * being rendered upside down. To resolve this, the image must be loaded
		 * into memory upside down.
		 * 
		 * Originally, Whirvis attempted to resolve this by inverting the UV
		 * coordinates in hopes of rendering the shape upside down (creating the
		 * desired effect of an upside-up image). This was recommended by
		 * answers to the same problem online.
		 * 
		 * However, it seems that when using glDrawElements(), inverting the UV
		 * coordinates changes nothing. This is probably due to the fact that
		 * glDrawElements() removes the need specify vertices in a counter
		 * clockwise order. This probably applies to UV as well.
		 */
		for (int y = img.getHeight() - 1; y >= 0; y--) {
			for (int x = 0; x < img.getWidth(); x++) {
				/*
				 * Since Java is Java, it returns pixels in an ARGB format.
				 * Since OpenGL has no option to store pixels in ARGB (it only
				 * supports RGBA), the pixels must be rearranged manually.
				 */
				int color = img.getRGB(x, y);
				buffer.put((byte) ((color >> 0x10) & 0xFF));
				buffer.put((byte) ((color >> 0x08) & 0xFF));
				buffer.put((byte) ((color >> 0x00) & 0xFF));
				buffer.put((byte) ((color >> 0x18) & 0xFF));
			}
		}
		buffer.flip();
		return buffer;
	}

	private final boolean preserveTexture;

	private final int h_glTexture;
	private final int h_glAttribs;
	private final int h_glVerts;

	private final Vector2i size;
	private final Vector2f scale;
	private final Vector2f pos;
	private final Vector4f color;

	private boolean closed;

	/**
	 * Constructs a new {@code Image} to handle and draw an OpenGL texture.
	 * 
	 * @param preserveTexture
	 *            {@code true} if the image should not be deleted when this
	 *            image is closed, {@code false} otherwise.
	 * @param h_glTexture
	 *            the handle of the OpenGL texture.
	 * @throws NoHandleException
	 *             if {@code h_glTexture} is {@code GL_NONE}.
	 */
	public Image(boolean preserveTexture, int h_glTexture) {
		init(); /* auto initialize components */
		this.preserveTexture = preserveTexture;

		this.h_glTexture = Handles.requireGL(h_glTexture, "h_glTexture");
		glBindTexture(GL_TEXTURE_2D, h_glTexture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		this.h_glAttribs =
				Handles.requireGL(glGenVertexArrays(), "h_glAttribs");
		glBindVertexArray(h_glAttribs);

		this.h_glVerts = Handles.requireGL(glGenBuffers(), "h_glVerts");
		glBindBuffer(GL_ARRAY_BUFFER, h_glVerts);
		glVertexAttribPointer(IMG_VERT_ID, 2, GL_FLOAT, false, 0, 0L);
		glEnableVertexAttribArray(IMG_VERT_ID);

		glBindBuffer(GL_ARRAY_BUFFER, h_glUV);
		glVertexAttribPointer(IMG_UV_ID, 2, GL_FLOAT, false, 0, 0L);
		glEnableVertexAttribArray(IMG_UV_ID);

		this.size = new Vector2i();
		this.scale = new Vector2f(1.0F, 1.0F);
		this.pos = new Vector2f();
		this.color = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

		/*
		 * Only increment the image count once the image has been successfully
		 * created. This will ensure close() de-initializes the image system
		 * even if another image fails to load.
		 */
		imgCount++;
	}

	/**
	 * Constructs a new {@code Image} to handle and draw an OpenGL texture.
	 * <p>
	 * The specified texture will not be deleted when this image is closed.<br>
	 * To change this, use {@link #Image(boolean, int)} with
	 * {@code preserveTexture} set to {@code false}.
	 * 
	 * @param h_glTexture
	 *            the handle of the OpenGL texture.
	 * @throws NoHandleException
	 *             if {@code h_glTexture} is {@code GL_NONE}.
	 */
	public Image(int h_glTexture) {
		this(true, h_glTexture);
	}

	/**
	 * Constructs a new {@code Image} and generates an OpenGL texture to handle
	 * and draw. The generated texture will be deleted automatically when this
	 * image is closed. To use one's own texture, use
	 * {@link #Image(boolean, int)}.
	 * 
	 * @throws NoHandleException
	 *             if the texture fails to generate.
	 */
	public Image() {
		this(false, glGenTextures());
	}

	private void requireOpen() {
		if (closed == true) {
			throw new IllegalStateException("image closed");
		}
	}

	/**
	 * Returns the image width.
	 * 
	 * @return the image width, in pixels.
	 */
	public int getWidth() {
		return size.x;
	}

	/**
	 * Returns the image height.
	 * 
	 * @return the image height, in pixels.
	 */
	public int getHeight() {
		return size.y;
	}

	/**
	 * Loads the vertices for the quad used to draw this image.
	 * <p>
	 * This is called automatically each time {@link #loadImage(BufferedImage)}
	 * is called. The arguments are set to the width and height of the image
	 * provided. While this can be used to resize the image, it should
	 * <b>not</b> be. To have the image drawn at different resolutions, please
	 * use {@link #setDrawSize(float, float)}.
	 * <p>
	 * If the image needs quad dimensions with no image, this is the function to
	 * use. An example would be if this image was being rendered to with an
	 * OpenGL framebuffer.
	 * 
	 * @param width
	 *            the quad width in pixels.
	 * @param height
	 *            the quad height in pixels.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image loadDimensions(int width, int height) {
		this.requireOpen();
		size.x = width;
		size.y = height;

		glBindBuffer(GL_ARRAY_BUFFER, h_glVerts);
		float[] dimensions = { 0, 0, 0, height, width, height, width, 0 };
		glBufferData(GL_ARRAY_BUFFER, dimensions, GL_STATIC_DRAW);
		return this;
	}

	/**
	 * Clears the vertices for the quad used to draw this image.
	 * 
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image clearDimensions() {
		this.requireOpen();
		glBindBuffer(GL_ARRAY_BUFFER, h_glVerts);
		glBufferData(GL_ARRAY_BUFFER, 0, GL_STATIC_DRAW);
		return this;
	}

	/**
	 * Loads the given {@link BufferedImage} as the image to draw.
	 * <p>
	 * The pixels from {@code image} are loaded into a buffer and set as the
	 * data for the texture this image handles. If {@code img} is {@code null},
	 * the contents of this image are cleared via {@link #clearDimensions()} and
	 * {@link #clearImage()}.
	 * <p>
	 * The dimensions are set to the width and height of {@code img}
	 * automatically.<br>
	 * To draw this image at a different size, use
	 * {@link #setDrawSize(float, float)}.
	 * 
	 * @param img
	 *            the image to load in.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image loadImage(BufferedImage img) {
		this.requireOpen();
		if (img == null) {
			this.clearDimensions();
			this.clearImage();
			return this;
		}
		this.loadDimensions(img.getWidth(), img.getHeight());

		ByteBuffer pixels = genPixBuf(img);
		glBindTexture(GL_TEXTURE_2D, h_glTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, size.x, size.y, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, pixels);
		pixels.clear(); /* ensure GC */
		return this;
	}

	/**
	 * Clears the pixels inside of the texture this image handles.
	 * <p>
	 * If the pixels were set via {@link #loadImage(BufferedImage)}, this will
	 * <b>not</b> clear the vertex buffer. To clear the vertex buffer also, use
	 * {@link #clearDimensions()}.
	 * 
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image clearImage() {
		this.requireOpen();
		glBindTexture(GL_TEXTURE_2D, h_glTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, size.x, size.y, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, 0);
		return this;
	}

	/**
	 * Sets the dimensions this image will draw at.
	 * 
	 * @param width
	 *            the width to draw this image at, in pixels.
	 * @param height
	 *            the height to draw this image at, in pixels.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image setDrawSize(float width, float height) {
		this.requireOpen();
		scale.x = width / size.x;
		scale.y = height / size.y;
		return this;
	}

	/**
	 * Sets the color of the image.
	 * 
	 * @param awtColor
	 *            the Java AWT color, may be {@code null}.
	 * @param alpha
	 *            {@code true} if the alpha of this image should also be updated
	 *            based on the alpha channel of {@code awtColor}, {@code false}
	 *            otherwise.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image setColor(Color awtColor, boolean alpha) {
		this.requireOpen();
		if (awtColor == null) {
			awtColor = Color.WHITE;
		}

		color.x = awtColor.getRed() / 255.0F;
		color.y = awtColor.getGreen() / 255.0F;
		color.z = awtColor.getBlue() / 255.0F;
		if (alpha) {
			color.w = awtColor.getAlpha() / 255.0F;
		}
		return this;
	}

	/**
	 * Sets the color and alpha of the image.
	 * <p>
	 * This function is a shorthand for {@link #setColor(Color, boolean)}, with
	 * the {@code alpha} parameter being set to {@code true}.
	 * 
	 * @param awtColor
	 *            the Java AWT color, may be {@code null}.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image setColor(Color awtColor) {
		return this.setColor(awtColor, true);
	}

	/**
	 * Sets the alpha of the image.
	 * <p>
	 * This updates the image alpha by updating the alpha channel of the image
	 * color.<br>
	 * This channel is also modified by {@link #setColor(Color)}.
	 * 
	 * @param alpha
	 *            the alpha channel.
	 * @return this image.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 */
	public Image setAlpha(float alpha) {
		this.requireOpen();
		color.w = alpha;
		return this;
	}

	/**
	 * Draws the image at the specified coordinates.
	 * <p>
	 * This function expects for an OpenGL image rendering program to be
	 * installed when this is called. It also expects that the uniforms for such
	 * shader have been resolved via {@link Program#resolveUniformLocs(Class)}.
	 * An orthographic matrix nd view matrix must also be set for the image to
	 * be drawn. These uniforms are also expected to be set before rendering.
	 * 
	 * @param x
	 *            the X-axis position, in pixels.
	 * @param y
	 *            the Y-axis positoin, in pixels.
	 * @throws IllegalStateException
	 *             if the image is closed.
	 * @see #setDrawSize(float, float)
	 * @see #setColor(Color)
	 * @see #setAlpha(float)
	 */
	@BeResponsible
	public void draw(float x, float y) {
		pos.x = x;
		pos.y = y;

		Program.setUniform(img_scale, scale);
		Program.setUniform(img_pos, pos);
		Program.setUniform(img_color, color);
		Program.setUniform(img_sampler, 0);

		glBindTexture(GL_TEXTURE_2D, h_glTexture);
		glActiveTexture(GL_TEXTURE0);

		glBindVertexArray(h_glAttribs);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, h_glIndices);
		glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0L);
	}

	@Override
	public void close() throws IOException {
		if (closed == true) {
			return;
		}

		if (!preserveTexture) {
			glDeleteTextures(h_glTexture);
		}
		glDeleteVertexArrays(h_glAttribs);
		glDeleteBuffers(h_glVerts);
		this.closed = true;

		imgCount--;
		if (imgCount <= 0) {
			deinit();
		}
	}

}
