package org.ardenus.engine.audio.sound.event;

import java.util.Objects;

import org.ardenus.engine.audio.AudioEvent;
import org.ardenus.engine.audio.sound.Sound;

/**
 * An event relating to a {@link Sound}.
 */
public class SoundEvent extends AudioEvent {

	private final Sound sound;

	/**
	 * Constructs a new {@code SoundEvent}.
	 * 
	 * @param sound
	 *            the sound that triggered the event.
	 * @throws NullPointerException
	 *             if {@code sound} is {@code null}.
	 */
	public SoundEvent(Sound sound) {
		this.sound = Objects.requireNonNull(sound, "sound");
	}

	/**
	 * Returns the sound that triggered the event.
	 * 
	 * @return the sound that triggered the event, guaranteed not to be
	 *         {@code null}.
	 */
	public Sound getSound() {
		return this.sound;
	}

}
