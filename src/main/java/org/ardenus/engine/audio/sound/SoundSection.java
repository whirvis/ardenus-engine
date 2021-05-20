package org.ardenus.engine.audio.sound;

/**
 * A portion of sound which shall be looped from a starting point to an end
 * point, with variations on behavior depending on the implemenation. This is
 * useful for looping certain tracks without needing to split a single audio
 * source into multiple tracks, {@code Sound} instances, etc.
 * 
 * @see StreamedSound#constrain(SoundSection)
 */
public class SoundSection {

	public final float start;
	public final float end;

	/**
	 * Constructs a new {@code SoundSection}.
	 * 
	 * @param start
	 *            the point at which to start the loop.
	 * @param duration
	 *            how long the loop shall last.
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
	 * Constructs a new {@code SoundSection}.
	 * <p>
	 * This constructor is a shorthand for {@link #SoundSection(float, float)}
	 * with the {@code startMillis} and {@code durationMillis} parameters being
	 * converted form milliseconds to seconds.
	 * 
	 * @param startMillis
	 *            the point in milliseconds at which to start the loop.
	 * @param durationMillis
	 *            how long in milliseconds the loop shall last.
	 * @throws IllegalArgumentException
	 *             if {@code startMillis} is negative or {@code durationMillis}
	 *             is not greater than zero.
	 */
	public SoundSection(long startMillis, long durationMillis) {
		this(startMillis / 1000.0F, durationMillis / 1000.0F);
	}

}
