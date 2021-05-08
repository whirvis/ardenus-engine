package org.ardenus.engine.audio.vorbis;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.audio.AudioSource;
import org.ardenus.engine.audio.sound.Sound;
import org.ardenus.engine.audio.sound.StreamedSound;
import org.lwjgl.system.MemoryStack;

/**
 * An OpenAL playable {@code OGG} vorbis file.
 */
public class VorbisFile implements AudioSource, Closeable {

	private static final int VORBIS_BITS_PER_SAMPLE = 16;
	
	public static void main(String[] args) throws Exception {
		Audio.init(null);
		
		Sound ss = new StreamedSound(new VorbisFile("C:/Users/Trent/Desktop/cb77_reaktion.ogg"));
		ss.play();
		
		Scanner console = new Scanner(System.in);
		while(ss.isPlaying()) {
			if(console.hasNextLine()) {
				String line = console.nextLine();
				float pos = Float.parseFloat(line);
				if(pos < 0) {
					ss.stop();
				}
				
				ss.setOffset(pos);
				System.out.println("Set position to " + pos + " seconds");
			}
		}
		Audio.terminate();
	}

	private static long vorbisOpen(File file) throws IOException {
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

	private File file;
	private final long h_stbVorbisDecoder;
	private VorbisInfo vorbisInfo;
	private int alFormat;
	private final Lock pcmLock;
	private int expectedOffset;

	/**
	 * Loads an {@code OGG} vorbis from a file on the system.
	 * 
	 * @param file
	 *            the vorbis file.
	 * @throws VorbisException
	 *             if a vorbis error occurs.
	 * @throws IOException
	 *             if an I/O error occurs while reading.
	 */
	public VorbisFile(File file) throws IOException {
		this.file = file;
		this.h_stbVorbisDecoder = vorbisOpen(file);
		this.vorbisInfo = VorbisInfo.get(h_stbVorbisDecoder);
		this.alFormat = alFormat(vorbisInfo);
		this.pcmLock = new ReentrantLock();
	}

	/**
	 * Loads an {@code OGG} vorbis from a file on the system.
	 * 
	 * @param path
	 *            the vorbis file path.
	 * @throws VorbisException
	 *             if a vorbis error occurs.
	 * @throws IOException
	 *             if an I/O error occurs while reading.
	 */
	public VorbisFile(String path) throws Exception {
		this(new File(path));
	}

	/**
	 * Returns the {@code OGG} vorbis file.
	 * 
	 * @return the file.
	 */
	public File getFile() {
		return this.file;
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
		pcmLock.lock();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			int channels = this.getChannelCount();

			/* only seek when necessary */
			if (expectedOffset != offset) {
				stb_vorbis_seek(h_stbVorbisDecoder,
						offset / (this.getBytesPerSample() * channels));
				this.expectedOffset = offset;
			}

			/*
			 * 
			 */
			ShortBuffer pcmBuf = MemoryStack.stackMallocShort(len / 2);
			pcmBuf.limit(stb_vorbis_get_samples_short_interleaved(
					h_stbVorbisDecoder, channels, pcmBuf) * channels);
			if (pcmBuf.limit() == 0) {
				return -1;
			}

			/*
			 * 
			 */
			int index = 0, size = pcmBuf.limit() * 2;
			while (index < size) {
				short sample = pcmBuf.get();
				buf[index++] = (byte) (sample & 0xFF);
				buf[index++] = (byte) ((sample >> 8) & 0xFF);
			}
			
			this.expectedOffset += size;
			return size;
		} finally {
			pcmLock.unlock();
		}
	}

	@Override
	public void close() {
		pcmLock.lock();
		try {
			stb_vorbis_close(h_stbVorbisDecoder);
		} finally {
			pcmLock.unlock();
		}
	}

}
