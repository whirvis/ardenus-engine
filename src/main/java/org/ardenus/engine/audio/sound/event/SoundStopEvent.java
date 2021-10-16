package org.ardenus.engine.audio.sound.event;

import org.ardenus.engine.audio.sound.Sound;

import com.whirvex.event.CancellableEvent;

public class SoundStopEvent extends SoundEvent implements CancellableEvent {

	private boolean cancelled;

	/**
	 * @param sound
	 *            the sound that has been stopped.
	 * @throws NullPointerException
	 *             if {@code sound} is {@code null}.
	 */
	public SoundStopEvent(Sound sound) {
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
