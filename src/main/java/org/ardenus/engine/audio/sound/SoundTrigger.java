package org.ardenus.engine.audio.sound;

import java.time.Duration;
import java.util.Objects;

import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.audio.sound.event.SoundTriggerEvent;

/**
 * Indicates when a certain point in time has been reached in a sound. This is
 * useful for adding in-game visual effects such as an alarm blaring, a piston
 * thrusting, etc.
 * <p>
 * <b>Note:</b> This should not be used to accomplishing looping sections of
 * audio! The margin of error for sound triggers going off at the intended
 * timestamp is highly erratic. As such, it should be used <i>only</i> for
 * cosmetic effects.
 * <p>
 * To achieve sectional looping, see {@link SoundSection}.
 * 
 * @see SoundTriggerEvent
 */
public final class SoundTrigger {

	public final long id;
	public final long[] timesMillis;
	private long lastOffsetMillis;
	private int triggerIndex;
	private Sound testing;

	/**
	 * @param id
	 *            the trigger ID. This is used to determine which sound trigger
	 *            was set off when listening for a {@link SoundTriggerEvent}.
	 * @param count
	 *            the trigger count, must be at least one.
	 * @param trigger
	 *            the point in time which this will trigger.
	 * @param tick
	 *            how long to wait between additional triggers after the initial
	 *            trigger, valid only if {@code count} is greater than one.
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
	 * @param id
	 *            the trigger ID. This is used to determine which sound trigger
	 *            was set off when listening for a {@link SoundTriggerEvent}.
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
	 * Tests if the sound trigger should trigger based on the offset of a sound.
	 * If so, a {@link SoundTriggerEvent} will be sent for each trigger which
	 * has yet to be fired.
	 * <p>
	 * <b>Note:</b> On first call, the argument for {@code sound} will be cached
	 * for future calls. If a different instance is passed in a future call, a
	 * {@code IllegalArgumentException} shall be thrown. This is because a sound
	 * trigger is intended for a single sound, and will break when tested
	 * against different sounds.
	 * 
	 * @param sound
	 *            the sound to trigger for.
	 * @param offsetMillis
	 *            the sound offset in milliseconds.
	 * @throws IllegalArgumentException
	 *             if a different instance is passed for {@code sound} after the
	 *             first call.
	 */
	protected void test(Sound sound, long offsetMillis) {
		if (testing == null) {
			this.testing = sound;
		} else if (testing != null && testing != sound) {
			throw new IllegalArgumentException(
					"already testing against another sound");
		}

		/*
		 * If the current offset is less than the last offset, that means the
		 * sound has been rewound. When this occurs, the triggerIndex must be
		 * updated to accommodate, or events will not be triggered again like
		 * they should. The new triggerIndex can be determined by finding the
		 * first trigger time the current offset is less than or equal to.
		 */
		if (offsetMillis < lastOffsetMillis) {
			for (int i = 0; i < timesMillis.length; i++) {
				if (offsetMillis <= timesMillis[i]) {
					this.triggerIndex = i;
					break;
				}
			}
		}
		this.lastOffsetMillis = offsetMillis;

		for (int i = triggerIndex; i < timesMillis.length; i++) {
			/*
			 * A negative delay signals that the trigger has not been reached
			 * yet. When this happens, break the loop early since that means
			 * none of the later triggers will be triggered either.
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
