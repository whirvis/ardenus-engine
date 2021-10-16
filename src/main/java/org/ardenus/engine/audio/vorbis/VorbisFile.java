package org.ardenus.engine.audio.vorbis;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardenus.engine.audio.AudioSource;
import org.lwjgl.system.MemoryStack;

/**
 * An OpenAL playable {@code OGG} Vorbis file.
 */
public class VorbisFile implements AudioSource, Closeable {

	private static final int VORBIS_BITS_PER_SAMPLE = 16;

	private static long vorbisOpen(File file) throws VorbisException {
		int[] p_error = new int[1];
		long h_stbVorbisDecoder =
				stb_vorbis_open_filename(file.getPath(), p_error, null);
		if (p_error[0] != VORBIS__no_error) {
			throw new VorbisException(p_error[0]);
		}
		return h_stbVorbisDecoder;
	}

	private static int alFormat(VorbisInfo info) {
		switch (info.channels) {
		case 1:
			return AL_FORMAT_MONO16;
		case 2:
			return AL_FORMAT_STEREO16;
		default:
			throw new UnsupportedOperationException("channel count");
		}
	}

	private final File file;
	private final long h_stbVorbisDecoder;
	private final VorbisInfo vorbisInfo;
	private final int alFormat;
	private final Lock decoderLock;
	private int expectedOffset;
	private boolean closed;

	/**
	 * Loads an {@code OGG} Vorbis from a file on the system.
	 * 
	 * @param file
	 *            the Vorbis file.
	 * @throws NullPointerException
	 *             if {@code file} is {@code null}.
	 * @throws VorbisException
	 *             if a Vorbis error occurs while opening {@code file}.
	 */
	public VorbisFile(File file) throws VorbisException {
		this.file = Objects.requireNonNull(file, "file");
		this.h_stbVorbisDecoder = vorbisOpen(file);
		this.vorbisInfo = VorbisInfo.get(this);
		this.alFormat = alFormat(vorbisInfo);
		this.decoderLock = new ReentrantLock();
	}

	/**
	 * Loads an {@code OGG} Vorbis from a file on the system.
	 * 
	 * @param path
	 *            the Vorbis file path.
	 * @throws NullPointerException
	 *             if {@code path} is {@code null}.
	 * @throws VorbisException
	 *             if a Vorbis error occurs while opening the file.
	 */
	public VorbisFile(String path) throws VorbisException {
		this(new File(path));
	}

	/**
	 * @return the {@code OGG} Vorbis file.
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return the STB Vorbis decoder handle.
	 * @throws VorbisException
	 *             if this Vorbis file has no decoder or is closed.
	 */
	protected long getDecoderHandle() throws VorbisException {
		if (h_stbVorbisDecoder == 0L) {
			throw new VorbisException("no decoder");
		} else if (closed == true) {
			throw new VorbisException("decoder closed");
		}
		return this.h_stbVorbisDecoder;
	}

	@Override
	public int getALFormat() {
		return this.alFormat;
	}

	@Override
	public int getFrequencyHz() {
		return vorbisInfo.sampleRate;
	}

	@Override
	public int getChannelCount() {
		return vorbisInfo.channels;
	}

	@Override
	public int getBitsPerSample() {
		return VORBIS_BITS_PER_SAMPLE;
	}

	@Override
	public int readPCM(int offset, byte[] buf, int len) throws IOException {
		if (closed == true) {
			throw new VorbisException("decoder closed");
		}

		decoderLock.lock();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			int channels = this.getChannelCount();
			int bytesPerSample = this.getBytesPerSample();

			/* only seek when necessary */
			if (expectedOffset != offset) {
				int sampleOffset = offset / (bytesPerSample * channels);
				stb_vorbis_seek(h_stbVorbisDecoder, sampleOffset);
				this.expectedOffset = offset;
			}

			/*
			 * A ShortBuffer must be used here, since using a ByteBuffer and
			 * passing it as a ShortBuffer using the asShortBuffer() method
			 * causes some whacky stuff to go down (audio cutting out for no
			 * reason, playback failing entirely, etc.) It is unknown why issues
			 * arise when this happens, but a ShortBuffer must be used here.
			 */
			int mallocAmt = len / Short.BYTES;
			ShortBuffer pcmBuf = MemoryStack.stackMallocShort(mallocAmt);
			pcmBuf.limit(stb_vorbis_get_samples_short_interleaved(
					h_stbVorbisDecoder, channels, pcmBuf) * channels);
			if (pcmBuf.limit() == 0) {
				return -1;
			}

			int index = 0;
			int size = pcmBuf.limit() * Short.BYTES;
			while (index < size) {
				short sample = pcmBuf.get();
				buf[index++] = (byte) (sample & 0xFF);
				buf[index++] = (byte) ((sample >> 8) & 0xFF);
			}

			this.expectedOffset += size;
			return size;
		} finally {
			decoderLock.unlock();
		}
	}

	@Override
	public void close() {
		if (closed == true) {
			return;
		}

		decoderLock.lock();
		try {
			stb_vorbis_close(h_stbVorbisDecoder);
			this.closed = true;
		} finally {
			decoderLock.unlock();
		}
	}

}
