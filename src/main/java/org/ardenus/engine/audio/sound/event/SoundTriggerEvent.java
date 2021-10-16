package org.ardenus.engine.audio.sound.event;

import java.util.Objects;

import org.ardenus.engine.audio.sound.Sound;
import org.ardenus.engine.audio.sound.SoundTrigger;

public class SoundTriggerEvent extends SoundEvent {

	private final SoundTrigger trigger;
	private final long index;
	private final long time;
	private final long delay;

	/**
	 * @param sound
	 *            the sound which was triggered for.
	 * @param trigger
	 *            the sound trigger.
	 * @param index
	 *            the trigger index.
	 * @param time
	 *            the intended trigger time.
	 * @param delay
	 *            how late the trigger was in milliseconds, if at all.
	 * @throws NullPointerException
	 *             if {@code sound} or {@code trigger} are {@code null}.
	 */
	public SoundTriggerEvent(Sound sound, SoundTrigger trigger, long index,
			long time, long delay) {
		super(sound);
		this.trigger = Objects.requireNonNull(trigger, "trigger");
		this.index = index;
		this.time = time;
		this.delay = delay;
	}

	/**
	 * @return the sound trigger.
	 */
	public SoundTrigger getTrigger() {
		return this.trigger;
	}

	/**
	 * @return the trigger ID.
	 * @see #getTrigger()
	 */
	public long getID() {
		return trigger.id;
	}

	/**
	 * Returns how many time this trigger has gone off in relation to the
	 * current offset of the sound. If the sound has been rewound (thus causing
	 * a trigger to go off again), the index will be lowered appropriately.
	 * 
	 * @return the trigger index.
	 */
	public long getIndex() {
		return this.index;
	}

	/**
	 * This returns the time the trigger <i>intended</i> to be triggered, not
	 * actually when it was triggered. To see how late the trigger was in being
	 * triggered, use {@link #getDelay()}. This should be used to compensate for
	 * lost time if necessary.
	 * 
	 * @return the intended trigger time in milliseconds.
	 */
	public long getTime() {
		return this.time;
	}

	/**
	 * @return the trigger delay in milliseconds.
	 */
	public long getDelay() {
		return this.delay;
	}

}
