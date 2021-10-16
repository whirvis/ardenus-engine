package org.ardenus.engine.audio.sound;

/**
 * A portion of sound which shall be looped from a starting point to an end
 * point, with variations on behavior depending on the implementation. This is
 * useful for looping certain tracks without needing to split a single audio
 * source into multiple tracks, instances of {@code Sound}, etc.
 * 
 * @see StreamedSound#constrain(SoundSection)
 */
public class SoundSection {

	public final float start;
	public final float end;

	/**
	 * @param start
	 *            the point at which to start, in seconds.
	 * @param duration
	 *            how long this section shall last, in seconds.
	 * @throws IllegalArgumentException
	 *             if {@code start} is negative or {@code duration} is not
	 *             greater than zero.
	 */
	public SoundSection(float start, float duration) {
		if (start < 0) {
			throw new IllegalArgumentException("start < 0");
		} else if (duration <= 0) {
			throw new IllegalArgumentException("duration <= 0");
		}
		this.start = start;
		this.end = start + duration;
	}

	/**
	 * This constructor is a shorthand for {@link #SoundSection(float, float)}
	 * with {@code startMillis} and {@code durationMillis} being converted from
	 * milliseconds to seconds.
	 * 
	 * @param startMillis
	 *            the point at which to start, in milliseconds.
	 * @param durationMillis
	 *            how long this section shall last, in milliseconds.
	 * @throws IllegalArgumentException
	 *             if {@code startMillis} is negative or {@code durationMillis}
	 *             is not greater than zero.
	 */
	public SoundSection(long startMillis, long durationMillis) {
		this(startMillis / 1000.0F, durationMillis / 1000.0F);
	}

}
