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
 * A {@code BufferedSound} should not be used to play large sound files, such as
 * music. They are intended for smaller audio samples, such as SFX. For larger
 * audio files, the usage of {@link StreamedSound} is recommended.
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
	 * @param audio
	 *            the audio source.
	 * @return the loadable {@code BufferedSound} resource.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 */
	public static Resource<BufferedSound> rsrc(AudioSource audio) {
		return new BufferedSoundResource(audio);
	}

	/**
	 * @param audio
	 *            the audio source.
	 * @return the generated buffer.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static ByteBuffer loadData(AudioSource audio) throws IOException {
		Objects.requireNonNull(audio, "audio");
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
	 * This method is a shorthand for {@link #loadData(AudioSource)}, with the
	 * data it loads being used to construct a new {@code BufferedSound}.
	 * 
	 * @param audio
	 *            the audio source.
	 * @return the sound just buffered into memory.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws SoundException
	 *             if the OpenAL source or buffer fails to generate.
	 * @see #loadData(AudioSource)
	 */
	public static BufferedSound load(AudioSource audio) throws IOException {
		ByteBuffer data = loadData(audio);
		return new BufferedSound(audio, data);
	}

	private final int h_alBuffer;
	private boolean closed;

	/**
	 * Constructs a new {@code BufferedSound} from an audio source and PCM data
	 * buffer for immediate playback. This constructor does not read PCM data
	 * from {@code audio}. It only describes the contents of {@code data}.
	 * 
	 * @param audio
	 *            the audio source.
	 * @param data
	 *            the loaded audio data buffer.
	 * @throws NullPointerException
	 *             if {@code audio} or {@code data} are {@code null}.
	 * @throws SoundException
	 *             if the OpenAL source or buffer fails to generate.
	 * @see #load(AudioSource)
	 */
	public BufferedSound(AudioSource audio, ByteBuffer data) {
		super(audio);
		Objects.requireNonNull(data, "data");

		this.h_alBuffer = alGenBuffers();
		if (h_alBuffer == AL_NONE) {
			throw new SoundException("failed to generate OpenAL buffer");
		}

		alBufferData(h_alBuffer, audio.getALFormat(), data,
				audio.getFrequencyHz());
		alSourcei(h_alSource, AL_BUFFER, h_alBuffer);
	}

	@Override
	public void close() throws IOException {
		if (closed == true) {
			return;
		}
		super.close();
		alDeleteBuffers(h_alBuffer);
		this.closed = true;
	}

}
