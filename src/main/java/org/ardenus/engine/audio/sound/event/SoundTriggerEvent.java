package org.ardenus.engine.audio.sound.event;

import java.util.Objects;

import org.ardenus.engine.audio.sound.Sound;
import org.ardenus.engine.audio.sound.SoundTrigger;

/**
 * Signals that a {@link SoundTrigger} has been triggered.
 */
public class SoundTriggerEvent extends SoundEvent {

	private final SoundTrigger trigger;
	private final long index;
	private final long time;
	private final long delay;

	/**
	 * Constructs a new {@code SoundTriggerEvent}.
	 * 
	 * @param sound
	 *            the sound which was triggered for.
	 * @param trigger
	 *            the sound trigger.
	 * @param index
	 *            the trigger index.
	 * @param time
	 *            the intended trigger time.
	 * @param delay
	 *            how late the trigger was, if at all.
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
	 * Returns the sound trigger.
	 * 
	 * @return the sound trigger, guaranteed not to be {@code null}.
	 */
	public SoundTrigger getTrigger() {
		return this.trigger;
	}

	/**
	 * Returns the trigger ID.
	 * 
	 * @return the trigger ID.
	 * @see #getTrigger()
	 */
	public long getID() {
		return trigger.id;
	}

	/**
	 * Returns how many time this trigger has been triggered.
	 * 
	 * @return the trigger index.
	 */
	public long getIndex() {
		return this.index;
	}

	/**
	 * Returns the time the trigger intended to be triggered.
	 * <p>
	 * <b>Note:</b> This only returns the time the trigger <i>intended</i> to be
	 * triggered, not actually when it was triggered. To see how late the
	 * trigger was in being triggered, use {@link #getDelay()}. This can be used
	 * to compensate for lost time if necessary.
	 * 
	 * @return the intended trigger time in milliseconds.
	 */
	public long getTime() {
		return this.time;
	}

	/**
	 * Returns how late the the trigger was in being triggered for this index,
	 * if at all.
	 * 
	 * @return the trigger delay was in milliseconds.
	 */
	public long getDelay() {
		return this.delay;
	}

}
