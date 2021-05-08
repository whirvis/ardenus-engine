package org.ardenus.engine.audio.wav;

import static org.lwjgl.openal.AL10.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardenus.engine.audio.AudioSource;
import org.ardenus.engine.io.riff.RiffChunkHeader;
import org.ardenus.engine.io.riff.RiffChunkInputStream;
import org.ardenus.engine.io.riff.RiffException;
import org.ardenus.engine.io.riff.RiffFile;

/**
 * An OpenAL playable {@code WAV} file, which is contained within the
 * {@link RiffFile RIFF} container file format.
 */
public class WaveFile extends RiffFile implements AudioSource {

	private static int alFormat(WaveFormat format) {
		switch (format.channelCount) {
			case 1:
				if (format.bitsPerSample == 8) {
					return AL_FORMAT_MONO8;
				} else if (format.bitsPerSample == 16) {
					return AL_FORMAT_MONO16;
				} else {
					throw new UnsupportedOperationException("bitrate");
				}
			case 2:
				if (format.bitsPerSample == 8) {
					return AL_FORMAT_STEREO8;
				} else if (format.bitsPerSample == 16) {
					return AL_FORMAT_STEREO16;
				} else {
					throw new UnsupportedOperationException("bitrate");
				}
			default:
				throw new UnsupportedOperationException("channel count");
		}
	}

	private final WaveFormat wavFormat;
	private final int alFormat;
	private final int dataSize;
	private final Lock pcmLock;
	private RiffChunkInputStream dataIn;
	private int expectedOffset;

	/**
	 * Loads a {@code WAV} file from a file on the system.
	 * <p>
	 * This constructor begins reading from the file, both to validate that
	 * {@code file} is a valid {@code RIFF} container and that it contains a
	 * valid {@code WAV} file.
	 * 
	 * @param file
	 *            the {@code RIFF} file.
	 * @throws RiffException
	 *             if {@code file} is not a valid {@code RIFF} container.
	 * @throws IOException
	 *             If an I/O error occurs while reading.
	 */
	public WaveFile(File file) throws IOException {
		super("WAVE", file);
		this.wavFormat = WaveFormat.read(this);
		this.alFormat = alFormat(wavFormat);
		this.pcmLock = new ReentrantLock();

		/* check against null for more accurate detail message */
		RiffChunkHeader dataHeader = this.getChunkHeader("data");
		if (dataHeader == null) {
			throw new RiffException("missing data chunk");
		}
		this.dataSize = dataHeader.size;
	}

	/**
	 * Loads a {@code WAV} file from a file on the system.
	 * <p>
	 * This constructor begins reading from the file, both to validate that the
	 * file at {@code path} is a valid {@code RIFF} container and that it
	 * contains a valid {@code WAV} file.
	 * 
	 * @param path
	 *            the {@code RIFF} file path.
	 * @throws RiffException
	 *             if {@code file} is not a valid {@code RIFF} container.
	 * @throws IOException
	 *             If an I/O error occurs while reading.
	 */
	public WaveFile(String path) throws IOException {
		this(new File(path));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Once the {@code data} chunk has been opened by
	 * {@link #readPCM(int, byte[], int)}, no other chunks can be opened for
	 * this {@code WAV} file. This is to prevent seemingly random closes of
	 * later opened {@code RIFF} chunk input streams.
	 */
	@Override
	public RiffChunkInputStream openChunk(String id) throws IOException {
		if (dataIn != null) {
			/*
			 * Once the data chunk has been opened, no other RIFF chunks can be
			 * opened. Doing so would close the internal data input stream,
			 * breaking the ability to read audio. Users of the WAV file must
			 * fetch all the information they need from chunks before the first
			 * call to readPCM().
			 */
			throw new IOException("opened data chunk");
		}
		return super.openChunk(id);
	}

	@Override
	public int getALFormat() {
		return this.alFormat;
	}

	@Override
	public int getFrequencyHz() {
		return wavFormat.sampleRate;
	}

	@Override
	public int getChannelCount() {
		return wavFormat.channelCount;
	}

	@Override
	public int getBitsPerSample() {
		return wavFormat.bitsPerSample;
	}

	@Override
	public long pcmLength() {
		return this.dataSize;
	}

	@Override
	public int readPCM(int offset, byte[] buf, int len) throws IOException {
		pcmLock.lock();
		try {
			/*
			 * Only open the data chunk when it is actually needed. This allows
			 * users to fetch other chunks from a WAV file before they start
			 * reading the PCM audio data if they so desire.
			 */
			if (dataIn == null) {
				this.dataIn = this.openChunk("data");
			}

			/* only seek when necessary */
			if (expectedOffset != offset) {
				dataIn.seek(offset);
				this.expectedOffset = offset;
			}

			int read = dataIn.read(buf, 0, len);
			this.expectedOffset += read;
			return read;
		} finally {
			pcmLock.unlock();
		}
	}

}
