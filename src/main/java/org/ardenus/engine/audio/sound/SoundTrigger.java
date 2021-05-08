package org.ardenus.engine.audio.sound;

import java.time.Duration;
import java.util.Objects;

import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.audio.sound.event.SoundTriggerEvent;

/**
 * A sound trigger, which indicates when a certain point in time has been
 * reached in a song. This is useful for adding in-game visual effects such as
 * an alarm blaring, a piston thrusting, etc.
 * <p>
 * <b>Note:</b> Sound triggers should be used <i>only</i> for cosmetic effects.
 */
public final class SoundTrigger {

	public final long id;
	public final long[] timesMillis;
	private int triggerIndex;

	/**
	 * Constructs a new {@code SoundTrigger}.
	 * 
	 * @param id
	 *            the trigger ID.
	 * @param count
	 *            the trigger count, must be at least one.
	 * @param trigger
	 *            the point in time which this will trigger.
	 * @param tick
	 *            how long to wait after the initial trigger, only if
	 *            {@code count} is greater than one.
	 * @throws NullPointerException
	 *             if {@code trigger} or {@code tick} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code count} is less than one, if {@code trigger} or
	 *             {@code tick} are negative, or {@code tick} is zero when
	 *             {@code count} is greater than one.
	 */
	public SoundTrigger(long id, int count, Duration trigger, Duration tick) {
		Objects.requireNonNull(trigger, "trigger");
		Objects.requireNonNull(tick, "tick");
		if (count < 1) {
			throw new IllegalArgumentException("count < 1");
		} else if (trigger.isNegative()) {
			throw new IllegalArgumentException("negative trigger");
		} else if (tick.isNegative()) {
			throw new IllegalArgumentException("negative tick");
		} else if (count > 1 && tick.isZero()) {
			throw new IllegalArgumentException("zero tick when count > 1");
		}
		this.id = id;

		/* cache trigger times for simplicity and efficiency */
		long triggerMs = trigger.toMillis();
		long tickMs = tick.toMillis();
		this.timesMillis = new long[count];
		for (int i = 0; i < timesMillis.length; i++) {
			this.timesMillis[i] = triggerMs + (tickMs * i);
		}
	}

	/**
	 * Constructs a new {@code SoundTrigger}.
	 * 
	 * @param id
	 *            the trigger ID.
	 * @param trigger
	 *            the point in time which this will trigger.
	 * @throws NullPointerException
	 *             if {@code trigger} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code trigger} is negative.
	 */
	public SoundTrigger(long id, Duration trigger) {
		this(id, 1, trigger, Duration.ZERO);
	}

	/**
	 * Tests if the sound trigger should trigger based on the milliseconds
	 * offset of a sound. If so, a {@link SoundTriggerEvent} will be sent for
	 * every trigger.
	 * <p>
	 * It should go without saying, only one instance of {@code Sound} should be
	 * calling this method. The instance in question should be the sound this
	 * trigger was registered to.
	 * 
	 * @param sound
	 *            the sound to trigger for.
	 * @param offsetMillis
	 *            the sound offset in milliseconds.
	 */
	protected void test(Sound sound, long offsetMillis) {
		for (int i = triggerIndex; i < timesMillis.length; i++) {
			/*
			 * A negative delay signals that the trigger has not been reached
			 * yet. When this happens, break the loop since that means none of
			 * the other triggers after this one will be triggered either.
			 */
			long triggerTime = timesMillis[i];
			long delay = offsetMillis - triggerTime;
			if (delay < 0) {
				break;
			}

			SoundTriggerEvent triggerEvent = new SoundTriggerEvent(sound, this,
					triggerIndex, triggerTime, delay);
			Audio.sendEvent(triggerEvent);
			triggerIndex++;
		}
	}

}
