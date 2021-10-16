package org.ardenus.engine.audio.sound.event;

import java.util.Objects;

import org.ardenus.engine.audio.AudioEvent;
import org.ardenus.engine.audio.sound.Sound;

public class SoundEvent extends AudioEvent {

	private final Sound sound;

	/**
	 * @param sound
	 *            the sound that triggered this event.
	 * @throws NullPointerException
	 *             if {@code sound} is {@code null}.
	 */
	public SoundEvent(Sound sound) {
		this.sound = Objects.requireNonNull(sound, "sound");
	}

	/**
	 * @return the sound that triggered this event.
	 */
	public Sound getSound() {
		return this.sound;
	}

}
