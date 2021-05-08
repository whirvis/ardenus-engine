package org.ardenus.engine.audio.wav;

import java.io.IOException;

import org.ardenus.engine.io.riff.RiffException;
import org.ardenus.engine.io.riff.RiffFile;
import org.ardenus.engine.io.riff.RiffInputStream;

/**
 * Contains format information about a {@link WaveFile WAV} file.
 */
public class WaveFormat {

	public static final int FORMAT_PCM = 1;

	/**
	 * Constructs a new {@code WaveFormat} by reading the parameters directly
	 * from a {@code RIFF} container.
	 * 
	 * @param file
	 *            the {@code RIFF} file.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return the {@code WAV} format container.
	 */
	public static WaveFormat read(RiffFile riff) throws IOException {
		RiffInputStream fmt = riff.openChunk("fmt ");

		short audioFormat = fmt.readShortLE();
		short channelCount = fmt.readShortLE();
		int sampleRate = fmt.readIntLE();
		int byteRate = fmt.readIntLE();
		short blockAlign = fmt.readShortLE();
		short bitsPerSample = fmt.readShortLE();

		short extraParamSize = 0;
		long extraParamPtr = -1L;
		if (audioFormat != FORMAT_PCM) {
			extraParamSize = fmt.readShortLE();
			extraParamPtr = fmt.ptr();
			if (fmt.skip(extraParamSize) < extraParamSize) {
				throw new RiffException("extra params larger than file");
			}
		}

		fmt.close();
		return new WaveFormat(audioFormat, channelCount, sampleRate, byteRate,
				blockAlign, bitsPerSample, extraParamSize, extraParamPtr);
	}

	public final short audioFormat;
	public final short channelCount;
	public final int sampleRate;
	public final int byteRate;
	public final short blockAlign;
	public final short bitsPerSample;
	public final short extraParamSize;

	/**
	 * A pointer to extra parameters of this {@code WAV} format. A value of
	 * {@code -1} indicates that no extra parameters are present.
	 */
	public final long extraParamPtr;

	public WaveFormat(short audioFormat, short channelCount, int sampleRate,
			int byteRate, short blockAlign, short bitsPerSample,
			short extraParamSize, long extraParamPtr) {
		this.audioFormat = audioFormat;
		this.channelCount = channelCount;
		this.sampleRate = sampleRate;
		this.byteRate = byteRate;
		this.blockAlign = blockAlign;
		this.bitsPerSample = bitsPerSample;
		this.extraParamSize = extraParamSize;
		this.extraParamPtr = extraParamPtr;
	}

}
