package org.ardenus.engine.audio.sound.event;

import org.ardenus.engine.audio.sound.Sound;

import com.whirvex.event.CancellableEvent;

/**
 * Signals that a {@link Sound} has begun playback.
 */
public class SoundPlayEvent extends SoundEvent implements CancellableEvent {

	private boolean cancelled;
	
	/**
	 * Constructs a new {@code SoundPlayEvent}.
	 * 
	 * @param sound
	 *            the sound that has begun playback.
	 * @throws NullPointerException
	 *             if {@code sound} is {@code null}.
	 */
	public SoundPlayEvent(Sound sound) {
		super(sound);
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
