package org.ardenus.engine.audio;

import java.util.Objects;

/**
 * A gain controller which individual sounds can be assigned to, allowing their
 * volume to be controlled in groups. The volume of a channel acts as a volume
 * multiplier for individual sounds. This makes the volume of a sound assigned
 * to a channel the following:
 * 
 * <pre>
 * 
 * float volume = sound.getVolume() * channel.getVolume();
 * </pre>
 * 
 * For example: if the volume of one sound is {@code 0.5F}, and the volume of
 * its channel is {@code 0.75F}, then its final volume will equal {@code 0.375F}
 * in playback.
 */
public class VolumeChannel {

	public final String id;
	public final String name;
	private float volume;

	/**
	 * @param id
	 *            the channel ID.
	 * @param name
	 *            the channel name, may be {@code null}.
	 * @param volume
	 *            the initial volume.
	 * @throws NullPointerException
	 *             if {@code id} is {@code null}.
	 */
	public VolumeChannel(String id, String name, float volume) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = name != null ? name : id;
		this.setVolume(volume);
	}

	/**
	 * Constructs a new {@code VolumeChannel} an initial volume of {@code 1.0F}.
	 * 
	 * @param id
	 *            the channel ID.
	 * @param name
	 *            the channel name, may be {@code null}.
	 * @throws NullPointerException
	 *             if {@code id} is {@code null}.
	 */
	public VolumeChannel(String id, String name) {
		this(id, name, 1.0F);
	}

	/**
	 * @return the channel volume, guaranteed to be between {@code 0.0F} and
	 *         {@code 1.0F}.
	 */
	public float getVolume() {
		return this.volume;
	}

	/**
	 * @param volume
	 *            the volume to set the channel to. This value will be capped
	 *            between {@code 0.0F} and {@code 1.0F}.
	 */
	public void setVolume(float volume) {
		if (volume < 0.0F) {
			this.volume = 0.0F;
		} else if (volume > 1.0F) {
			this.volume = 1.0F;
		} else {
			this.volume = volume;
		}
	}

}
