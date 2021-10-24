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
 * @see #init(EventManager)
 */
public class Audio {

	protected static final Logger LOG = LogManager.getLogger(Audio.class);

	private static boolean initialized;
	private static EventManager events;
	private static long device;
	private static long context;
	private static AudioThread audioThread;

	/**
	 * @param eventManager
	 *            the event manager, may be {@code null}.
	 * @throws AudioException
	 *             if the OpenAL context could not be made current.
	 * @see #sendEvent(AudioEvent)
	 */
	public static void init(EventManager eventManager) {
		if (initialized == true) {
			LOG.error("Already initialized");
			return;
		}

		events = EventManager.valueOf(eventManager);

		LOG.info("Opening default device...");
		device = alcOpenDevice((String) null);
		context = alcCreateContext(device, (int[]) null);
		if (!alcMakeContextCurrent(context)) {
			throw new AudioException("failed to make context current");
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

	protected static void requireInit() {
		if (initialized == false) {
			throw new IllegalStateException("not initialized");
		}
	}

	/**
	 * @param <T>
	 *            the event type.
	 * @param event
	 *            the audio event.
	 * @return {@code event} as passed.
	 * @throws IllegalStateException
	 *             if the audio system is not initialized.
	 * @throws NullPointerException
	 *             if {@code event} is {@code null}.
	 */
	public static <T extends AudioEvent> T sendEvent(T event) {
		Audio.requireInit();
		return events.send(event);
	}

	/**
	 * Maintains a sound by having it automatically updated on the audio
	 * system's thread.<br>
	 * This is called automatically by a {@code Sound} when initialized.
	 * 
	 * @param sound
	 *            the sound to maintain.
	 * @throws IllegalStateException
	 *             if the audio system has not been initialized.
	 * @throws NullPointerException
	 *             if {@code sound} is {@code null}.
	 */
	public static void maintain(Sound sound) {
		Audio.requireInit();
		audioThread.maintain(sound);
	}

	/**
	 * Abandons a sound and ensures that it is no longer updated by the audio
	 * system's thread.<br>
	 * This is called automatically by a {@code Sound} when closed.
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

	public static void terminate() {
		if (initialized == false) {
			LOG.error("Already terminated");
			return;
		}

		/*
		 * The audio thread must be stopped first, otherwise sounds will get
		 * updated after crucial audio systems have been shut down.
		 */
		LOG.info("Stopping thread...");
		audioThread.interrupt();

		LOG.info("Closing device...");
		alcDestroyContext(context);
		alcCloseDevice(device);

		initialized = false;
		LOG.info("Terminated system");
	}

}
