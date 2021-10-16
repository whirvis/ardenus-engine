package org.ardenus.engine.util.fade;

import java.time.Duration;
import java.util.Objects;

/**
 * Used for the transition of one value to another.
 */
public abstract class Fade {

	public final double start;
	public final double finish;
	public final double change;
	public final Duration time;

	/* use double for floating point division */
	protected final double timeMillis;

	/**
	 * @param start
	 *            the starting value.
	 * @param finish
	 *            the finishing value.
	 * @param time
	 *            how long the fade should endure.
	 * @throws NullPointerException
	 *             if {@code time} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code time} is negative.
	 */
	public Fade(double start, double finish, Duration time) {
		this.start = start;
		this.finish = finish;
		this.change = finish - start;
		this.time = Objects.requireNonNull(time, "time");
		if (time.isNegative()) {
			throw new IllegalArgumentException("negative time");
		}

		this.timeMillis = time.toMillis();
	}

	/**
	 * Implementation for {@link #atOffset(long)}.
	 * 
	 * @param offsetMillis
	 *            the offset in milliseconds.
	 * @return the value of {@code offsetMillis}.
	 */
	protected abstract double atOffset0(long offsetMillis);

	/**
	 * Calculates the value of the fade at a given offset.
	 * <p>
	 * If the offset is negative, the starting value will be returned. If the
	 * offset is greater than the duration of the fade, the finishing value will
	 * be returned. Anything in between will have the value calculated for that
	 * offset.
	 * 
	 * @param offsetMillis
	 *            the offset in milliseconds.
	 * @return the value at {@code offsetMillis}.
	 */
	public final double atOffset(long offsetMillis) {
		if (offsetMillis <= 0) {
			return this.start;
		} else if (offsetMillis >= timeMillis) {
			return this.finish;
		}
		return this.atOffset0(offsetMillis);
	}

	/**
	 * Calculates the value of the fade at a given offset.
	 * <p>
	 * If the offset is negative, the starting value will be returned. If the
	 * offset is greater than the duration of the fade, the finishing value will
	 * be returned. Anything in between will have the value calculated for that
	 * offset.
	 * 
	 * @param offset
	 *            the offset.
	 * @return the value at {@code offsetMillis}.
	 */
	public final double atOffset(Duration offset) {
		return this.atOffset(offset.toMillis());
	}

}
