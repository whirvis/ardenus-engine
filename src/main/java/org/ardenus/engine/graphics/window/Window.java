package org.ardenus.engine.graphics.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.Closeable;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.input.Input;
import org.ardenus.engine.input.UnicodeInputEvent;
import org.ardenus.engine.input.device.InputDevice;
import org.ardenus.engine.input.device.Keyboard;
import org.ardenus.engine.input.device.Mouse;
import org.ardenus.engine.input.device.Ps4Controller;
import org.ardenus.engine.input.device.Ps5Controller;
import org.ardenus.engine.input.device.SwitchProController;
import org.ardenus.engine.input.device.XboxController;
import org.ardenus.engine.input.device.seeker.DeviceSeeker;
import org.ardenus.engine.input.device.seeker.GlfwDeviceSeeker;
import org.ardenus.engine.input.device.seeker.GlfwKeyboardSeeker;
import org.ardenus.engine.input.device.seeker.GlfwMouseSeeker;
import org.ardenus.engine.input.device.seeker.GlfwPs4Seeker;
import org.ardenus.engine.input.device.seeker.GlfwPs5Seeker;
import org.ardenus.engine.input.device.seeker.GlfwSwitchProSeeker;
import org.ardenus.engine.input.device.seeker.GlfwXboxSeeker;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

/**
 * A window created and managed via <a href="https://www.glfw.org">GLFW</a>.
 */
public class Window implements Closeable {

	private static final Logger LOG = LogManager.getLogger(Window.class);

	private static boolean initialized;
	private static String version;

	/**
	 * Initializes the window system.
	 * <p>
	 * A GLFW error callback is automatically set by this method via
	 * {@link org.lwjgl.glfw.GLFW#glfwSetErrorCallback(GLFWErrorCallbackI)
	 * glfwSetErrorCallback(GLFWErrorCallbackI)} after initialization. This
	 * error callback simply logs GLFW errors to the console.
	 * <p>
	 * <b>Note:</b> After initialization, {@link #pollEvents()} must be called
	 * each program update. If this is neglected, all created windows will
	 * become unresponsive to the operating system.
	 * 
	 * @throws WindowException
	 *             if GLFW fails to initialize.
	 */
	public static void init() {
		if (initialized == true) {
			LOG.error("Already initialized");
			return;
		}

		LOG.info("Initializing GLFW...");
		if (!glfwInit()) {
			throw new WindowException("failed to initialize GLFW");
		}

		LOG.info("Setting GLFW error callback...");
		glfwSetErrorCallback((error, description) -> LOG.error(
				"Error " + error + ": " + MemoryUtil.memASCII(description)));

		initialized = true;
		LOG.info("Initialized windows");
	}

	/**
	 * Terminates the window system.
	 * <p>
	 * If the window system has not been initialized (or previously terminated
	 * before another initialization), then this method will do nothing.
	 */
	public static void terminate() {
		if (initialized == false) {
			LOG.error("Already terminated");
			return;
		}

		LOG.info("Terminating GLFW...");
		glfwTerminate();

		initialized = false;
		LOG.info("Terminated windows");
	}

	/**
	 * Polls and updates GLFW.
	 * <p>
	 * This method is only a shorthand for calling
	 * {@link org.lwjgl.glfw.GLFW#glfwPollEvents() glfwPollEvents()}.
	 */
	public static void pollEvents() {
		glfwPollEvents();
	}

	/**
	 * Enables V-Sync for all windows.
	 */
	public static void enableVSync() {
		glfwSwapInterval(1);
	}

	/**
	 * Disables V-Sync for all windows.
	 */
	public static void disableVSync() {
		glfwSwapInterval(0);
	}

	/**
	 * @return the current version of GLFW.
	 */
	public static String getVersion() {
		if (version == null) {
			int[] major = new int[1];
			int[] minor = new int[1];
			int[] revision = new int[1];
			glfwGetVersion(major, minor, revision);
			version = major[0] + "." + minor[0] + "." + revision[0];
		}
		return version;
	}

	private final long ptr_glfwWindow;

	private int x, y;
	private int width, height;
	private String title;
	private boolean destroyed;

	/**
	 * Creates a GLFW window.
	 * 
	 * @param width
	 *            the window width.
	 * @param height
	 *            the window height.
	 * @param title
	 *            the window title, a {@code null} value is permitted and will
	 *            have the title set to an empty string.
	 * @throws IllegalStateException
	 *             if windows have not been initialized via {@link #init()}.
	 * @throws IllegalArgumentException
	 *             if {@code width} or {@code height} are negative.
	 */
	public Window(int width, int height, String title) {
		if (initialized == false) {
			throw new IllegalStateException("windows not initialized");
		} else if (width < 0 || height < 0) {
			throw new IllegalArgumentException("width < 0 || height < 0");
		}

		this.ptr_glfwWindow = glfwCreateWindow(width, height, "", 0, 0);

		this.width = width;
		this.height = height;
		this.setTitle(title);

		glfwSetWindowSizeCallback(ptr_glfwWindow, (w, cw, ch) -> {
			this.width = cw;
			this.height = ch;
		});
		glfwSetWindowPosCallback(ptr_glfwWindow, (w, cx, cy) -> {
			this.x = cx;
			this.y = cy;
		});
	}

	/**
	 * @return the X position of the window.
	 */
	public int getX() {
		return this.x;
	}

	/**
	 * @return the Y position of the window.
	 */
	public int getY() {
		return this.y;
	}

	/**
	 * Sets the position of the window relative to the monitors.
	 * 
	 * @param x
	 *            the X position.
	 * @param y
	 *            the Y position.
	 */
	public void setPosition(int x, int y) {
		glfwSetWindowPos(ptr_glfwWindow, x, y);
	}

	/**
	 * Returns the current window width.
	 * 
	 * @return the current window width.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the current window height.
	 * 
	 * @return the current window height.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Sets the window size.
	 * 
	 * @param width
	 *            the width.
	 * @param height
	 *            the height.
	 * @throws IllegalArgumentException
	 *             if {@code width} or {@code height} are negative.
	 */
	public void setSize(int width, int height) throws IllegalArgumentException {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("width < 0 || height < 0");
		}
		glfwSetWindowSize(ptr_glfwWindow, width, height);
	}

	/**
	 * Returns the window title.
	 * 
	 * @return the window title, may be {@code null}.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Sets window title.
	 * 
	 * @param title
	 *            the window title, a {@code null} value is permitted and will
	 *            have the title set to an empty string.
	 */
	public void setTitle(String title) {
		this.title = title;
		glfwSetWindowTitle(ptr_glfwWindow, title != null ? title : "");
	}

	/**
	 * Returns if the window is focused.
	 * 
	 * @return {@code true} if this window is focused, {@code false} otherwise.
	 */
	public boolean isFocused() {
		return glfwGetWindowAttrib(ptr_glfwWindow, GLFW_FOCUSED) == GLFW_TRUE;
	}

	/**
	 * Returns if the window is decorated.
	 * 
	 * @return {@code true} if this window is decorated, {@code false}
	 *         otherwise.
	 */
	public boolean isDecorated() {
		return glfwGetWindowAttrib(ptr_glfwWindow, GLFW_FOCUSED) == GLFW_TRUE;
	}

	/**
	 * Sets whether or not the window should be decorated.
	 * 
	 * @param decorated
	 *            {@code true} if this window should be decorated, {@code false}
	 *            otherwise.
	 */
	public void setDecorated(boolean decorated) {
		glfwSetWindowAttrib(ptr_glfwWindow, GLFW_DECORATED,
				decorated ? GL_TRUE : GL_FALSE);
	}

	/**
	 * Returns if the window is resizable.
	 * 
	 * @return {@code true} if this window is resizable, {@code false}
	 *         otherwise.
	 */
	public boolean isResizable() {
		return glfwGetWindowAttrib(ptr_glfwWindow, GLFW_RESIZABLE) == GLFW_TRUE;
	}

	/**
	 * Sets whether or not window should be resizable.
	 * 
	 * @param resizable
	 *            {@code true} if this window should be resizable, {@code false}
	 *            otherwise.
	 */
	public void setResizable(boolean resizable) {
		glfwSetWindowAttrib(ptr_glfwWindow, GLFW_RESIZABLE,
				resizable ? GL_TRUE : GL_FALSE);
	}

	/**
	 * Returns if the window is floating.
	 * 
	 * @return {@code true} if this window is floating, {@code false} otherwise.
	 */
	public boolean isFloating() {
		return glfwGetWindowAttrib(ptr_glfwWindow, GLFW_FLOATING) == GLFW_TRUE;
	}

	/**
	 * Sets whether or not the window should be floating.
	 * 
	 * @param floating
	 *            {@code true} if this window should be floating, {@code false}
	 *            otherwise.
	 */
	public void setFloating(boolean floating) {
		glfwSetWindowAttrib(ptr_glfwWindow, GLFW_FLOATING,
				floating ? GL_TRUE : GL_FALSE);
	}

	/**
	 * Returns if the window will focus when shown.
	 * 
	 * @return {@code true} if this window will focus when shown, {@code false}
	 *         otherwise.
	 */
	public boolean focusesOnShow() {
		return glfwGetWindowAttrib(ptr_glfwWindow,
				GLFW_FOCUS_ON_SHOW) == GLFW_TRUE;
	}

	/**
	 * Sets whether or not the window should focus when shown.
	 * 
	 * @param focusOnShow
	 *            {@code true} if this window should focus when shown,
	 *            {@code false} otherwise.
	 */
	public void setFocusOnShow(boolean focusOnShow) {
		glfwSetWindowAttrib(ptr_glfwWindow, GLFW_FOCUS_ON_SHOW,
				focusOnShow ? GL_TRUE : GL_FALSE);
	}

	/**
	 * Shows the window.
	 */
	public void show() {
		glfwShowWindow(ptr_glfwWindow);
	}

	/**
	 * Hides the window.
	 */
	public void hide() {
		glfwHideWindow(ptr_glfwWindow);
	}

	/**
	 * Minimizes the window.
	 */
	public void minimize() {
		glfwIconifyWindow(ptr_glfwWindow);
	}

	/**
	 * Maximizes the window.
	 */
	public void maximimze() {
		glfwRestoreWindow(ptr_glfwWindow);
	}

	/**
	 * Binds the window's GLFW context to the calling thread.
	 */
	public void makeContextCurrent() {
		glfwMakeContextCurrent(ptr_glfwWindow);
	}

	/**
	 * Swaps the window buffers from the current buffer to the next rendered
	 * buffer.
	 */
	public void swapBuffers() {
		glfwSwapBuffers(ptr_glfwWindow);
	}

	/**
	 * @return {@code true} if the window has been requested to close,
	 *         {@code false} otherwise.
	 */
	public boolean shouldClose() {
		return glfwWindowShouldClose(ptr_glfwWindow);
	}

	/**
	 * Sets whether or not the window should close.
	 * 
	 * @param shouldClose
	 *            {@code true} if this window should close, {@code false}
	 *            otherwise.
	 */
	public void setShouldClose(boolean shouldClose) {
		glfwSetWindowShouldClose(ptr_glfwWindow, shouldClose);
	}

	/**
	 * Tells the window the send unicode input it receives to the input system.
	 * <p>
	 * This <i>must</i> be called after the input system has been
	 * initialized.<br>
	 * Otherwise, an {@code IllegalStateException} may be thrown!
	 * 
	 * @see UnicodeInputEvent
	 */
	public void registerUnicodeInput() {
		Window window = this;
		glfwSetCharCallback(ptr_glfwWindow, (w, c) -> {
			Input.sendEvent(new UnicodeInputEvent(window, (char) c));
		});
	}

	/**
	 * Tells the window to stop sending unicode input it receives to the input
	 * system.
	 * <p>
	 * This <i>must</i> be called immediately before the input system is
	 * terminated.<br>
	 * Otherwise, an {@code IllegalStateException} may be thrown!
	 */
	public void stopUnicodeInput() {
		glfwSetCharCallback(ptr_glfwWindow, null);
	}

	/**
	 * Creates a device seeker for this window.
	 * 
	 * @param type
	 *            the input device type.
	 * @return the created device seeker.
	 * @throws NullPointerException
	 *             if {@code type} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code type} is unsupported.
	 * @see Input#addSeeker(DeviceSeeker)
	 */
	public GlfwDeviceSeeker createSeeker(Class<? extends InputDevice> type) {
		Objects.requireNonNull(type, "type");
		if (type == Keyboard.class) {
			return new GlfwKeyboardSeeker(ptr_glfwWindow);
		} else if (type == Mouse.class) {
			return new GlfwMouseSeeker(ptr_glfwWindow);
		} else if (type == XboxController.class) {
			return new GlfwXboxSeeker(ptr_glfwWindow);
		} else if (type == Ps4Controller.class) {
			return new GlfwPs4Seeker(ptr_glfwWindow);
		} else if (type == Ps5Controller.class) {
			return new GlfwPs5Seeker(ptr_glfwWindow);
		} else if (type == SwitchProController.class) {
			return new GlfwSwitchProSeeker(ptr_glfwWindow);
		} else {
			throw new IllegalArgumentException("unsupported device");
		}
	}

	/**
	 * Create device seekers for this window.
	 * 
	 * @param types
	 *            the input device types.
	 * @return the created device seekers.
	 * @throws NullPointerException
	 *             if {@code types} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if any types are unsupported.
	 * @see Input#addSeekers(DeviceSeeker...)
	 */
	@SuppressWarnings("unchecked")
	public GlfwDeviceSeeker[]
			createSeekers(Class<? extends InputDevice>... types) {
		Objects.requireNonNull(types, "types");
		GlfwDeviceSeeker[] seekers = new GlfwDeviceSeeker[types.length];
		for (int i = 0; i < seekers.length; i++) {
			seekers[i] = this.createSeeker(types[i]);
		}
		return seekers;
	}

	/**
	 * @return {@code true} if this window has been destroyed, {@code false}
	 *         otherwise.
	 * @see #close()
	 */
	public boolean isDestroyed() {
		return this.destroyed;
	}

	/**
	 * Destroys the window.
	 */
	@Override
	public void close() {
		if (this.isDestroyed()) {
			return;
		}
		glfwDestroyWindow(ptr_glfwWindow);
		this.destroyed = true;
	}

}
