package org.ardenus.engine.audio;

import static org.lwjgl.openal.ALC10.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.audio.sound.Sound;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import com.whirvex.event.EventManager;

/**
 * The audio system for the Ardenus Engine.
 * 
 * @see Sound
 * @see AudioListener
 * @see Audio#init()
 */
public class Audio {

	private static final Logger LOG = LogManager.getLogger(Audio.class);

	private static boolean initialized;
	private static EventManager manager;
	private static long device;
	private static long context;
	private static AudioThread audioThread;

	/**
	 * Initializes the audio system.
	 * 
	 * @param eventManager
	 *            the event manager, may be {@code null}.
	 * @see #sendEvent(AudioEvent)
	 */
	public static void init(EventManager eventManager) {
		if (initialized == true) {
			LOG.error("Audio already initialized");
			return;
		}

		manager = EventManager.valueOf(eventManager);

		LOG.info("Opening default device...");
		device = alcOpenDevice((String) null);
		context = alcCreateContext(device, (int[]) null);
		if (!alcMakeContextCurrent(context)) {
			throw new RuntimeException("failed to make context current");
		}

		LOG.info("Creating capabilities...");
		ALCCapabilities capabilities = ALC.createCapabilities(device);
		AL.createCapabilities(capabilities);

		LOG.info("Starting thread...");
		audioThread = new AudioThread();
		audioThread.start();

		initialized = true;
		LOG.info("Initialized system");
	}

	/**
	 * Sends an {@link AudioEvent} to the audio system's event manager.
	 * 
	 * @param <T>
	 *            the event type.
	 * @param event
	 *            the audio event.
	 * @return {@code event} as passed.
	 */
	public static <T extends AudioEvent> T sendEvent(T event) {
		return manager.send(event);
	}

	/**
	 * Maintains a sound by having it automatically updated on the audio
	 * system's thread. Called automatically by {@code Sound} when initialized.
	 * 
	 * @param sound
	 *            the sound to maintain.
	 * @throws IllegalStateException
	 *             if the audio system has not been initialized.
	 */
	public static void maintain(Sound sound) {
		Audio.requireInit();
		audioThread.maintain(sound);
	}

	/**
	 * Abandons a sound and has it no longer updated by the audio system's
	 * thread. Called automatically by {@code Sound} when closed.
	 * 
	 * @param sound
	 *            the sound to abandon.
	 * @throws IllegalStateException
	 *             if the audio system has not been initialized.
	 */
	public static void abandon(Sound sound) {
		Audio.requireInit();
		audioThread.abandon(sound);
	}

	protected static void requireInit() {
		if (initialized == false) {
			throw new IllegalStateException("not initialized");
		}
	}

	/**
	 * Terminates the audio system.
	 * <p>
	 * If the audio system has not been initialized (or previously terminated
	 * before another initialization), then this method will do nothing.
	 */
	public static void terminate() {
		if (initialized == false) {
			LOG.error("Already terminated");
			return;
		}

		LOG.info("Closing device...");
		alcDestroyContext(context);
		alcCloseDevice(device);

		LOG.info("Stopping thread...");
		audioThread.interrupt();

		initialized = false;
		LOG.info("Terminated system");
	}

}
