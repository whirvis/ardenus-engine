package org.ardenus.engine.audio;

import java.util.Objects;

/**
 * A volume channel which sounds can be assigned to, allowing their volume to be
 * controlled in groups.
 * <p>
 * The volume of a channel acts as a volume multiplier for individual sounds.
 * This makes the volume of a sound assigned to a channel the following:
 * 
 * <pre>
 * 
 * float volume = sound.getVolume() * channel.getVolume();
 * </pre>
 * 
 * For example, if the volume of one sound is {@code 0.5F}, and the volume of
 * its channel is {@code 0.75F}, then its final volume will equal {@code 0.375F}
 * in playback.
 */
public final class VolumeChannel {

	public final String id;
	public final String name;
	private float volume;

	/**
	 * Creates a volume channel.
	 * 
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
	 * Creates a volume channel with an initial volume of {@code 1.0F}.
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
	 * Returns the channel volume.
	 * 
	 * @return the channel volume, guaranteed to be between {@code 0.0F} and
	 *         {@code 1.0F}.
	 */
	public float getVolume() {
		return this.volume;
	}

	/**
	 * Sets the channel volume.
	 * 
	 * @param volume
	 *            the volume, will be capped between {@code 0.0F} and
	 *            {@code 1.0F} if outside of that range.
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

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "VolumeChannel [id=" + id + ", name=" + name + ", volume="
				+ volume + "]";
	}

}
