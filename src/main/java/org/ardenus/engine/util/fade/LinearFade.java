package org.ardenus.engine.util.fade;

import java.time.Duration;

/**
 * An implementation of {@code Fade} which uses linear interpolation.
 */
public class LinearFade extends Fade {

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
	public LinearFade(double start, double finish, Duration time) {
		super(start, finish, time);
	}

	@Override
	protected double atOffset0(long offsetMillis) {
		return start + change * (offsetMillis / timeMillis);
	}

}
