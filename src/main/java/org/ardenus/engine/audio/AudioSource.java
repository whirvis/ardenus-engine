package org.ardenus.engine.audio;

import java.io.IOException;

/**
 * A source of audio that read PCM data.
 * 
 * @see #getALFormat()
 * @see #readPCM(int, byte[], int)
 */
public interface AudioSource {

	/**
	 * Returns the OpenAL format.
	 * <p>
	 * The format shall consist of {@code AL_FORMAT_MONO8},
	 * {@code AL_FORMAT_MONO16}, {@code AL_FORMAT_STEREO8} or
	 * {@code AL_FORMAT_STEREO16}. If the format of the file cannot be
	 * determined due to a previous error, an
	 * {@code UnsupportedOperationException} shall be thrown.
	 * 
	 * @return the format.
	 */
	public int getALFormat();

	/**
	 * Returns the frequency in Hz.
	 * <p>
	 * The frequency represents how many samples will play a second (regardless
	 * of the amount of channels). For example, a frequency of {@code 44.1Khz}
	 * means that {@code 44,100} samples will play each second.
	 * 
	 * @return the frequency in Hz.
	 */
	public int getFrequencyHz();

	/**
	 * Returns the channel count.
	 * <p>
	 * Channels are used to determine which speakers audio will play on
	 * (assuming the audio source is multi-channeled). Most audio is either
	 * <i>mono</i> (one channel) or <i>stereo</i> (two channels). Some audio can
	 * be surround surround (four or more channels), but this is rare and
	 * usually not supported by the system.
	 * 
	 * @return the channel count.
	 */
	public int getChannelCount();

	/**
	 * Returns the bitrate per sample.
	 * <p>
	 * The bits per sample how many bits are in each sample, which determines
	 * the overall quality of the audio. Audio must be either {@code 8-bit} or
	 * {@code 16-bit}, per the OpenAL standard. This information is necessary to
	 * determine the length of audio (if supported) as well as current position.
	 * 
	 * @return the bitrate per sample.
	 */
	public int getBitsPerSample();

	/**
	 * Returns the byterate per sample.
	 * <p>
	 * The bytes per sample how many bytes are in each sample, which determines
	 * the overall quality of the audio. Audio must be either {@code 8-bit} or
	 * {@code 16-bit}, per the OpenAL standard. This information is necessary to
	 * determine the length of audio (if supported) as well as current position.
	 * <p>
	 * This method is a shorthand for {@link #getBitsPerSample()} with the
	 * return value being returned by {@link Byte#SIZE}.
	 * 
	 * @return the byterate per sample.
	 */
	public default int getBytesPerSample() {
		return this.getBitsPerSample() / Byte.SIZE;
	}

	/**
	 * Returns the size of the total PCM data in bytes, if supported.
	 * <p>
	 * Whether or not this is supported is dependent on the class implementing
	 * {@code AudioSource} capabilities. Most audio sources reading from a file
	 * can return how many bytes of PCM there are to read. However, sources for
	 * audio like voice chat or radio cannot.
	 * 
	 * @return the size of the entire PCM in bytes.
	 * @throws UnsupportedOperationException
	 *             if not implemented.
	 */
	public default long pcmLength() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempts to read PCM data from the audio source into {@code buf}.
	 * <p>
	 * The amount of data that actually gets read may be less than requested.
	 * This can be due to decompression limitations, not enough data being
	 * present in the file from {@code offset}, or some other issue.
	 * 
	 * @param offset
	 *            the offset in bytes.
	 * @param buf
	 *            the buffer to read into.
	 * @param len
	 *            the number of bytes to attempt to read.
	 * @return the actual number of bytes read, or {@code -1} if the end of the
	 *         audio source has been reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int readPCM(int offset, byte[] buf, int len) throws IOException;

}
