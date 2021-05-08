package org.ardenus.engine.audio.sound;

import static org.lwjgl.openal.AL10.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.ardenus.engine.audio.AudioSource;
import org.ardenus.engine.io.Resource;
import org.lwjgl.BufferUtils;

/**
 * A playable sound that has all of its audio data buffered into memory at once.
 * <p>
 * This should not be used to play larger sound files, such as music. A
 * {@code BufferedSound} should be used only for short audio samples (such as
 * SFX). To play longer sound files, a {@link StreamedSound} should be used
 * instead.
 */
public class BufferedSound extends Sound {

	private static class BufferedSoundResource extends Resource<BufferedSound> {

		private final AudioSource audio;
		private ByteBuffer buffer;
		private BufferedSound sound;

		public BufferedSoundResource(AudioSource audio) {
			this.audio = Objects.requireNonNull(audio, "audio");
		}

		@Override
		public void offload() throws IOException {
			this.buffer = BufferedSound.loadData(audio);
		}

		@Override
		public void load() {
			this.sound = new BufferedSound(audio, buffer);
		}

		@Override
		public BufferedSound getLoaded() {
			return this.sound;
		}

	}

	/**
	 * Creates a loadable {@code BufferedSound} resource.
	 * 
	 * @param audio
	 *            the audio source.
	 * @return the loadable {@code BufferedSound}.
	 */
	public static Resource<BufferedSound> rsrc(AudioSource audio) {
		return new BufferedSoundResource(audio);
	}

	/**
	 * Loads all audio data from {@code audio} into an audio data buffer.
	 * 
	 * @param audio
	 *            the audio source.
	 * @return the generated buffer.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static ByteBuffer loadData(AudioSource audio) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] chunk = new byte[1024];
		int read = 0, size = 0;
		while ((read = audio.readPCM(size, chunk, chunk.length)) != -1) {
			out.write(chunk, 0, read);
			size += read;
		}

		/*
		 * OpenAL requires that all buffer sizes be a multiple of four. This
		 * single line ensures that requirement is met by trimming off the few
		 * bytes needed. With how little is being cut off, it won't be
		 * noticeable by the listener.
		 * 
		 * Assuming the buffer is 16-bit PCM stereo, which is 176,400 bytes per
		 * second, the most bytes that can be lost due to this (3 bytes), is
		 * only 1/50th of a millisecond:
		 * 
		 * (3b / 176,400b) * 1000ms = 0.01700680272ms
		 */
		size -= (size % 4);

		ByteBuffer pcmBuffer = BufferUtils.createByteBuffer(size);
		pcmBuffer.put(out.toByteArray(), 0, size).flip();
		return pcmBuffer;
	}

	/**
	 * Loads the audio data from {@code audio} into memory and constructs a new
	 * {@code BufferedSound} from it for immediate playback.
	 * 
	 * @param audio
	 *            the audio source.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @see #loadData(AudioSource)
	 */
	public static BufferedSound load(AudioSource audio) throws IOException {
		ByteBuffer data = loadData(audio);
		return new BufferedSound(audio, data);
	}

	private final int h_alBuffer;

	/**
	 * Constructs a new {@code BufferedSound} from an audio source and PCM data
	 * buffer for immediate playback. This constructor does not read PCM data
	 * from {@code audio}. It only describes the contents of {@code data}.
	 * 
	 * @param audio
	 *            the audio source.
	 * @param data
	 *            the loaded audio data buffer.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @see #load(AudioSource)
	 */
	public BufferedSound(AudioSource audio, ByteBuffer data) {
		super(audio);
		this.h_alBuffer = alGenBuffers();
		alBufferData(h_alBuffer, audio.getALFormat(), data,
				audio.getFrequencyHz());
		alSourcei(h_alSource, AL_BUFFER, h_alBuffer);
	}

	@Override
	public void close() throws IOException {
		super.close();
		alDeleteBuffers(h_alBuffer);
	}

}
