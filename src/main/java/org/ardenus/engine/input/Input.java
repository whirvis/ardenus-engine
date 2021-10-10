package org.ardenus.engine.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.input.device.InputDevice;
import org.ardenus.engine.input.device.seeker.DeviceSeeker;

import com.whirvex.event.EventManager;

/**
 * The input system for the Ardenus Engine.
 * 
 * @see #init(EventManager)
 */
public class Input {

	protected static final Logger LOG = LogManager.getLogger(Input.class);

	private static boolean initialized;
	private static EventManager events;
	private static Map<Class<?>, DeviceSeeker> seekers;
	private static List<InputDevice> connected;

	/**
	 * Initializes the input system.
	 * 
	 * @param eventManager
	 *            the event manager, may be {@code null}.
	 * @see #sendEvent(InputEvent)
	 */
	public static void init(EventManager eventManager) {
		if (initialized == true) {
			LOG.error("Already initialized");
			return;
		}

		events = EventManager.valueOf(eventManager);
		seekers = new HashMap<>();
		connected = new ArrayList<>();

		initialized = true;
		LOG.info("Initalized system");
	}

	protected static void requireInit() {
		if (initialized == false) {
			throw new IllegalStateException("not initialized");
		}
	}

	/**
	 * Sends an {@link InputEvent} to the input system's event manager.
	 * 
	 * @param <T>
	 *            the event type.
	 * @param event
	 *            the input event.
	 * @return {@code event} as passed.
	 * @throws IllegalStateException
	 *             if the input system is not initialized.
	 * @throws NullPointerException
	 *             if {@code event} is {@code null}.
	 */
	public static <T extends InputEvent> T sendEvent(T event) {
		Input.requireInit();
		return events.send(event);
	}

	/**
	 * Returns if the input system has a device seeker for a controller type.
	 * 
	 * @param type
	 *            the controller type.
	 * @return {@code true} if the input system has a device seeker registered
	 *         for {@code type}, {@code false} otherwise.
	 */
	public static boolean hasSeeker(Class<?> type) {
		if (type != null && seekers != null) {
			return seekers.containsKey(type);
		}
		return false;
	}

	/**
	 * Adds a device seeker to the input system.
	 * <p>
	 * Once a device seeker has been added, it will be polled automatically
	 * alongside the rest of the input system (when {@link #poll()} is called.)
	 * <p>
	 * <b>Note:</b> No two device seekers may seek for the same type of input
	 * device. As such, if a previous device seeker was added for the same
	 * controller type that {@code seeker} scans for, it shall be removed
	 * automatically. This is to prevent situations arising where one input
	 * device gets registered as two input devices in the same application.
	 * 
	 * @param seeker
	 *            the device seeker.
	 * @throws IllegalStateException
	 *             if the input system is not initialized.
	 * @throws NullPointerException
	 *             if {@code seeker} is {@code null}.
	 * @see #connected()
	 */
	public static void addSeeker(DeviceSeeker seeker) {
		Input.requireInit();
		Objects.requireNonNull(seeker, "seeker");

		/*
		 * Before overriding the previous device seeker for the controller type,
		 * make sure to explicitly remove it. This is done to remove any
		 * lingering input devices that are still considered connected.
		 */
		removeSeeker(seeker.type);
		seekers.put(seeker.type, seeker);
	}

	/**
	 * Removes a device seeker from the input system.
	 * <p>
	 * Once a device seeker is removed, any devices that would have been
	 * returned by {@link #connected()} and were registered by {@code seeker}
	 * will no longer be returned. While the devices in question may still be
	 * connected, this is done out of intuition.
	 * 
	 * @param type
	 *            the controller type to stop seeking.
	 */
	public static void removeSeeker(Class<?> type) {
		if (type == null || seekers == null) {
			return;
		}
		DeviceSeeker removed = seekers.remove(type);
		if (removed != null) {
			for (InputDevice device : removed.registered()) {
				connected.remove(device);
			}
		}
	}

	/**
	 * Removes a device seeker from the input system.
	 * <p>
	 * Once a device seeker is removed, any devices that would have been
	 * returned by {@link #connected()} and were registered by {@code seeker}
	 * will no longer be returned. While the devices in question may still be
	 * connected, this is done out of intuition.
	 * <p>
	 * This method is mostly a shorthand for {@link #removeSeeker(Class)} with
	 * {@code type} being equal to {@code seeker.type}. However, a check is
	 * performed to ensure that the current device seeker registered to
	 * {@code seeker.type} is equal to {@code seeker}. Once this is verified,
	 * the call to {@link #removeSeeker(Class)} will be made.
	 * 
	 * @param seeker
	 *            the device seeker to stop polling.
	 */
	public static void removeSeeker(DeviceSeeker seeker) {
		if (seeker == null || seekers == null) {
			return;
		}
		DeviceSeeker current = seekers.get(seeker.type);
		if (current == seeker) {
			removeSeeker(seeker.type);
		}
	}

	/**
	 * Returns all currently connected input devices.
	 * <p>
	 * For an input device to appear in this list, it first must have been found
	 * by a registered device seeker. Secondly, it must return that it is in
	 * this moment connected via its {@link InputDevice#isConnected()} method.
	 * If an input device later reports that it is no longer connected, it will
	 * disappear from this list. If a device seeker is unregistered, all input
	 * devices that it registered will disappear from this list (whether or not
	 * they are connected.)
	 * 
	 * @return all currently connected input devices, {@code null} if the input
	 *         system is not initialized.
	 * @see #addSeeker(DeviceSeeker)
	 */
	public static List<InputDevice> connected() {
		if (connected == null) {
			return null;
		}
		return Collections.unmodifiableList(connected);
	}

	private static void pollSeeker(DeviceSeeker seeker) {
		seeker.poll();

		for (InputDevice device : seeker.registered()) {
			boolean isConnected = device.isConnected();
			boolean wasConnected = connected.contains(device);

			if (isConnected && !wasConnected) {
				connected.add(device);
			} else if (!isConnected && wasConnected) {
				connected.remove(device);
			}
		}
	}

	/**
	 * Polls the input system.
	 * <p>
	 * Polling the input system is necessary for retrieving up to date input
	 * information. If this is not done, it is possible a mix of both up to date
	 * and out of date input data will be returned. As such, it is recommended
	 * to call this method once every update.
	 * 
	 * @throws IllegalStateException
	 *             if the input system is not initialized.
	 */
	public static void poll() {
		Input.requireInit();
		for (DeviceSeeker seeker : seekers.values()) {
			pollSeeker(seeker);
		}
	}

	/**
	 * Terminates the input system.
	 * <p>
	 * If the input system has not been initialized (or previously terminated
	 * before another initialization), then this method will do nothing.
	 */
	public static void terminate() {
		if (initialized == false) {
			LOG.error("Already terminated");
			return;
		}

		/* ensure garbage collection */
		LOG.info("Nullifying storage");
		seekers = null;
		connected = null;

		initialized = false;
		LOG.info("Terminated system");
	}

}
