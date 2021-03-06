package org.ardenus.engine.audio.sound;

import static org.lwjgl.openal.AL10.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.audio.AudioSource;
import org.lwjgl.BufferUtils;

/**
 * A playable sound which has audio data buffered into memory as it is played.
 * <p>
 * A {@code StreamedSound} should not be used to play small sound files, such as
 * SFX. They are intended for larger audio samples, such as music or narration.
 * For smaller audio files, the usage of {@link BufferedSound} is recommended.
 */
public class StreamedSound extends Sound {

	private static final Logger LOG = LogManager.getLogger(StreamedSound.class);
	private static final int BUF_MODULO = 4; /* see #fillAndQueue() */
	private static final int MIN_BUFSIZE = 4096; /* usually just enough */
	private static final int MAX_BUFSIZE = 176400; /* 1s of 16-bit stereo */
	private static final Lock BUFSIZE_LOCK = new ReentrantLock();

	private static int minBufSize = MIN_BUFSIZE;
	private static boolean warnedMaxBuf;

	/**
	 * @return the minimum buffer size for each instance of
	 *         {@code StreamedSound}.
	 * @see #setMinBufferSize(int)
	 */
	public static int getMinBufferSize() {
		return minBufSize;
	}

	/**
	 * This guarantees the internal buffer size of each {@code StreamedSound} is
	 * no smaller than {@code bufSize}. If the current buffer size is less than
	 * {@code bufSize} it will be increased accordingly on the next update.
	 * <p>
	 * At one time the minimum buffer size was local to each instance of a
	 * {@code StreamedSound}. However, it was determined that if one instance
	 * was falling behind in playback and needed an increase, it is likely all
	 * the others would fall behind also.
	 * 
	 * @param bufSize
	 *            the new minimum buffer size. Due to OpenAL requirements, this
	 *            will be increased to a multiple of four if it is not already.
	 * @throws IllegalArgumentException
	 *             if {@code bufSize} is less than {@value #MIN_BUFSIZE} or
	 *             greater than {@value #MAX_BUFSIZE}.
	 */
	public static void setMinBufferSize(int bufSize) {
		if (bufSize < MIN_BUFSIZE) {
			throw new IllegalArgumentException("bufSize < " + MIN_BUFSIZE);
		} else if (bufSize > MAX_BUFSIZE) {
			throw new IllegalArgumentException("bufSize > " + MAX_BUFSIZE);
		}

		BUFSIZE_LOCK.lock();
		try {
			bufSize += bufSize % BUF_MODULO;
			minBufSize = bufSize;
		} finally {
			BUFSIZE_LOCK.unlock();
		}
	}

	private static int getOffsetBytes(AudioSource audio, float offset) {
		int offsetBytes = (int) (offset * audio.getChannelCount()
				* audio.getFrequencyHz() * audio.getBytesPerSample());
		offsetBytes -= offsetBytes % BUF_MODULO;
		return offsetBytes;
	}

	private static void increaseMinBufSize() {
		int recoupSize = minBufSize * 2;
		if (recoupSize < MAX_BUFSIZE) {
			setMinBufferSize(recoupSize);
			LOG.warn("Audio is falling behind!"
					+ " Increased minimum buffer size to " + minBufSize
					+ " bytes");
		} else if (warnedMaxBuf == false) {
			LOG.error("Audio is falling behind even at max buffer size!"
					+ " Is there an issue with the storage medium?");
			warnedMaxBuf = true; /* only warn once */
		}
	}

	private final int[] h_alBuffers;
	private final Lock alReadLock;
	private final Lock alWriteLock;

	private byte[] readBuf;
	private int readPos;
	private boolean updateOffset;
	private ByteBuffer heapBuf;
	private int processedBytes;

	/*
	 * An internal alState and looping variable variable are used so intended
	 * state of this sound can be returned back to the caller. For example,
	 * isPlaying() should still return true even when the sound intermittently
	 * stops due to slow buffer loading. This is because the sound is going to
	 * be restarted as soon as the next buffer is loaded.
	 * 
	 * Traditional OpenAL looping cannot be used since streamed sounds load the
	 * next buffer as the current one is playing. Sometimes the buffer will fail
	 * to load before the current playing buffer stops. When this happens, the
	 * sound must stop to indicate this.
	 * 
	 * Additional note: While requireOpen() is manually called for some
	 * overriding methods in this class, it is not done for others. That is
	 * because these overriding methods call their corresponding super method,
	 * which call requireOpen() themselves (TL;DR it would be redundant).
	 */
	private int alState;
	private boolean looping;
	private boolean initialize;
	private boolean closed;

	private SoundSection sect;
	private int sectStartBytes;
	private int sectEndBytes;
	private int sectLenBytes;
	private boolean sectEnded;

	/**
	 * Prepares an audio source for streamed playback.
	 * 
	 * @param audio
	 *            the audio source.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 */
	public StreamedSound(AudioSource audio) throws IOException {
		super(audio, false);

		this.h_alBuffers = new int[3];
		for (int i = 0; i < h_alBuffers.length; i++) {
			int h_alBuffer = alGenBuffers();
			if (h_alBuffer == AL_NONE) {
				throw new SoundException("failed to generate OpenAL buffer");
			}
			this.h_alBuffers[i] = h_alBuffer;
		}

		ReadWriteLock alLock = new ReentrantReadWriteLock();
		this.alReadLock = alLock.readLock();
		this.alWriteLock = alLock.writeLock();

		this.alState = AL_INITIAL;
		this.setBufferSize(minBufSize);
		Audio.maintain(this);
	}

	/**
	 * Calling this will destroy the internal {@code readBuf} and
	 * {@code heapBuf} and all of their contained data. The new buffers will
	 * have a size of {@code bufSize}.
	 * 
	 * @param bufSize
	 *            the new buffer size. Due to OpenAL requirements, this will be
	 *            increased to a multiple of four if it is not already.
	 * @throws IllegalArgumentException
	 *             if {@code bufSize} is lower than {@code minBufSize}.
	 */
	private void setBufferSize(int bufSize) {
		if (bufSize < minBufSize) {
			throw new IllegalArgumentException("bufSize < minBufSize");
		}
		bufSize += bufSize % BUF_MODULO;
		this.readBuf = new byte[bufSize];
		this.heapBuf = BufferUtils.createByteBuffer(bufSize);
	}

	private boolean inState(int state) {
		this.requireOpen();
		alReadLock.lock();
		try {
			return alState == state;
		} finally {
			alReadLock.unlock();
		}
	}

	@Override
	public boolean isPlaying() {
		return this.inState(AL_PLAYING);
	}

	@Override
	public void play() {
		this.requireOpen();
		alWriteLock.lock();
		try {
			if (this.isPlaying()) {
				this.stop();
				this.initialize = true;
			} else {
				/*
				 * If the sound is paused, that means the audio data buffers
				 * have already been initialized. Initializing the buffers again
				 * would result in a portion of the sound being skipped over.
				 */
				this.initialize = !this.isPaused();
			}
			super.play();
			this.alState = AL_PLAYING;
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public boolean isPaused() {
		return this.inState(AL_PAUSED);
	}

	@Override
	public void pause() {
		this.requireOpen();
		if (this.isPaused()) {
			return;
		}

		alWriteLock.lock();
		try {
			/*
			 * The sound can only be paused if it was already playing. This
			 * check is not only to mimic existing OpenAL behavior. It is
			 * necessary since it determines whether or not the audio data
			 * buffers should be initialized when the sound is played.
			 */
			if (this.isPlaying()) {
				super.pause();
				this.alState = AL_PAUSED;
			}
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public boolean isStopped() {
		return this.inState(AL_STOPPED);
	}

	@Override
	public void stop() {
		this.requireOpen();
		if (this.isStopped()) {
			return;
		}

		alWriteLock.lock();
		try {
			super.stop();
			alSourceUnqueueBuffers(h_alSource, h_alBuffers);

			this.readPos = 0;
			this.processedBytes = 0;
			this.alState = AL_STOPPED;
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public boolean isLooping() {
		this.requireOpen();
		alReadLock.lock();
		try {
			return this.looping;
		} finally {
			alReadLock.unlock();
		}
	}

	@Override
	public void setLooping(boolean looping) {
		this.requireOpen();
		alWriteLock.lock();
		try {
			this.looping = looping;
		} finally {
			alWriteLock.unlock();
		}
	}

	/**
	 * This increases/decreases the size of {@code readBuf} in accordance to
	 * {@code pitch} if necessary. The new size is based on {@code minBufSize},
	 * which is defined during construction. Any value of {@code pitch} below
	 * {@code 1.0F} will set the size of {@code readBuf} to {@code minBufSize}.
	 * <p>
	 * <b>Note:</b> If the buffer size is increased as a result of calling this
	 * method, it will not be decreased even if the pitch is lowered later. This
	 * may be changed in the future, however there is no guarantee for this.
	 */
	@Override
	public void setPitch(float pitch) {
		this.requireOpen();
		alWriteLock.lock();
		try {
			/*
			 * The pitch affects how fast the song is played. As such, if the
			 * pitch is set to anything above 1.0F, the buffer size (not the
			 * minimum buffer size) should be increased to accomodate the higher
			 * speed of the sound if necessary.
			 */
			if (pitch > 1.0F) {
				int pitchBufSize = (int) Math.ceil(minBufSize * pitch);
				if (pitchBufSize > readBuf.length) {
					this.setBufferSize(pitchBufSize);
				}
			}

			/*
			 * Only update the pitch after the buffer sizes have been
			 * appropriately increased. Updating the pitch beforehand may result
			 * in the sound stopping intermittently, resulting in a false
			 * warning.
			 */
			super.setPitch(pitch);
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public int getByteOffset() {
		alReadLock.lock();
		try {
			int currentBytes = super.getByteOffset();
			return processedBytes + currentBytes;
		} finally {
			alReadLock.unlock();
		}
	}

	@Override
	public void setByteOffset(int byteOffset) {
		if (byteOffset < 0) {
			throw new IndexOutOfBoundsException("byteOffset < 0");
		}

		alWriteLock.lock();
		try {
			byteOffset -= byteOffset % BUF_MODULO;
			this.readPos = byteOffset;
			this.processedBytes = byteOffset;
			this.updateOffset = true;
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public int getSampleOffset() {
		alReadLock.lock();
		try {
			int currentSamples = super.getSampleOffset();
			int processedSamples = processedBytes
					/ (audio.getBytesPerSample() * audio.getChannelCount());
			return processedSamples + currentSamples;
		} finally {
			alReadLock.unlock();
		}
	}

	@Override
	public void setSampleOffset(int sampleOffset) {
		if (sampleOffset < 0) {
			throw new IndexOutOfBoundsException("sampleOffset < 0");
		}
		this.setByteOffset(sampleOffset * audio.getBytesPerSample()
				* audio.getChannelCount());
	}

	@Override
	public float getOffset(boolean wholeSeconds) {
		float sampleOffset = this.getSampleOffset();
		float offset = sampleOffset / audio.getFrequencyHz();
		return wholeSeconds ? (float) Math.floor(offset) : offset;
	}

	/**
	 * This can be used both for sounds which loop and sounds which do not
	 * loop.<br>
	 * It is recommended that constraints be set before playback begins.
	 * 
	 * @param sect
	 *            the section of audio to play, may be {@code null} to remove
	 *            the current constraint (if any) and resume full playback.
	 * @return this sound.
	 */
	public StreamedSound constrain(SoundSection sect) {
		this.requireOpen();
		alWriteLock.lock();
		try {
			this.sect = sect;
			if (sect == null) {
				this.sectStartBytes = 0;
				this.sectEndBytes = 0;
				this.sectLenBytes = 0;
				this.sectEnded = false;
				return this;
			}

			this.sectStartBytes = getOffsetBytes(audio, sect.start);
			this.sectEndBytes = getOffsetBytes(audio, sect.end);
			this.sectLenBytes = sectEndBytes - sectStartBytes;

			/* not sure who would do this, but whatever. */
			if (sectLenBytes < BUF_MODULO) {
				this.sectEndBytes = sectStartBytes + BUF_MODULO;
				this.sectLenBytes = BUF_MODULO;
			}
			return this;
		} finally {
			alWriteLock.unlock();
		}
	}

	/**
	 * The portion of audio data as well as the amount of data filled into the
	 * buffer is determined by the value of {@code readPos} and the length of
	 * {@code readBuf}. If not enough data is present to fill {@code heapBuf}
	 * entirely, only the required slice is piped into OpenAL.
	 * <p>
	 * In the event that the end of the audio data stream is reached, if looping
	 * has been enabled via {@link #setLooping(boolean)}, then the
	 * {@code readPos} cursor will be reset to zero so the song can immediately
	 * restart. Otherwise, the {@code heapBuf} will not be filled, nothing will
	 * be queued, and {@code false} will be returned.
	 * 
	 * @param h_alBuffer
	 *            the OpenAL buffer to fill with data and queue.
	 * @return {@code true} if {@code h_alBuffer} was filled and queued,
	 *         {@code false} otherwise.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private boolean fillAndQueue(int h_alBuffer) throws IOException {
		int read = audio.readPCM(readPos, readBuf, readBuf.length);
		if (read == -1) {
			if (this.isLooping()) {
				this.setByteOffset(0);
				return this.fillAndQueue(h_alBuffer);
			}
			return false;
		}

		/*
		 * OpenAL requires that all buffer sizes be a multiple of four. This
		 * single line ensures that requirement is met by trimming off the few
		 * bytes needed. With how little is being cut off, it won't be
		 * noticeable by the listener.
		 * 
		 * Assuming the buffer is 16-bit stereo PCM, which is 176,400 bytes per
		 * second, the most bytes that can be lost due to this (3 bytes), is
		 * only 1/50th of a millisecond:
		 * 
		 * (3b / 176,400b) * 1000ms = 0.01700680272ms
		 */
		read -= (read % BUF_MODULO);
		this.readPos += read;

		/*
		 * The current loop has been completed. Chop off the the execess data by
		 * so as not to play past what should be looped.
		 */
		if (sect != null && readPos >= sectEndBytes) {
			read -= readPos - sectEndBytes;
			read -= (read % BUF_MODULO);
			this.readPos = sectStartBytes;
			this.processedBytes = sectStartBytes;

			if (!this.isLooping()) {
				return false;
			}
			this.sectEnded = true;
		}

		/*
		 * The contents from readBuf must be pushed onto heapBuf, as readBuf is
		 * inaccessible by OpenAL. The readBuf is used for compatibility with
		 * the AudioSource interface. Using a cached buffer that is then pushed
		 * onto a single heap buffer also saves memory usage.
		 */
		heapBuf.clear();
		heapBuf.put(readBuf, 0, read).flip();
		ByteBuffer pipe = heapBuf.slice();

		alBufferData(h_alBuffer, audio.getALFormat(), pipe,
				audio.getFrequencyHz());
		alSourceQueueBuffers(h_alSource, h_alBuffer);
		return true;
	}

	@Override
	public void update() throws IOException {
		this.requireOpen();
		alWriteLock.lock();
		try {
			if (readBuf.length < minBufSize) {
				this.setBufferSize(minBufSize);
			}

			/*
			 * This must be done before any other operations on the source or
			 * buffers. If readPos has been updated by an outside force, restart
			 * the sound to have it reinitialize at the new position. With how
			 * fast this occurs (and within a write lock), it will sound as
			 * though the sound was never restarted.
			 */
			if (updateOffset == true) {
				this.updateOffset = false;

				/* read position can only be set when playing */
				if (this.isPlaying()) {
					int offset = this.readPos;
					this.play(); /* play resets readPos */
					this.readPos = offset;
				} else {
					this.readPos = 0;
				}
			}

			/*
			 * Initialize all buffers at once since the sound has just been
			 * started. This ensures a more stable initial playback.
			 */
			if (initialize == true) {
				this.initialize = false;
				for (int h_alBuffer : h_alBuffers) {
					this.fillAndQueue(h_alBuffer);
				}
				super.play();
			}

			/*
			 * If looping a section, before queuing any buffers, ensure that the
			 * readPos is at least at the start of the loop section. The later
			 * buffer processing will catch when the end of the loop section has
			 * been reached.
			 */
			if (sect != null && readPos < sectStartBytes) {
				this.setByteOffset(sectStartBytes);
			}

			int processed = alGetSourcei(h_alSource, AL_BUFFERS_PROCESSED);
			while (processed-- > 0) {
				int h_alBuffer = alSourceUnqueueBuffers(h_alSource);
				this.processedBytes += alGetBufferi(h_alBuffer, AL_SIZE);
				if (!this.fillAndQueue(h_alBuffer)) {
					this.stop(); /* no more data */
				}
			}

			/*
			 * When this sound should be playing but the super indicates it is
			 * not, that means the sound must be started again. However, the
			 * reason as to why the sound is not playing is also important.
			 * 
			 * If a section has finished playing, the sound being stopped is
			 * likely the result of the read buffers being trimmed. Read buffers
			 * are trimmed to ensure only the intended section is played. When
			 * this happens, there's no need to increase the minimum buffer size
			 * as the audio isn't actually falling behind.
			 * 
			 * If the current section has not ended (either because it was still
			 * being played, or because no section was being played at all), it
			 * indicates the buffer size is too small. When the buffer size is
			 * too small, there isn't enough time to fill the next buffer with
			 * before before the current buffer is finished playing. To fix
			 * this, increase the minimum buffer size if possible.
			 * 
			 * Once everything is put in order, restart the sound via super so
			 * as not to restart it from the beginning.
			 */
			if (this.isPlaying() && !super.isPlaying()) {
				if (sectEnded == false) {
					increaseMinBufSize();
				}
				this.sectEnded = false;
				super.play();
			}
			super.update();
		} finally {
			alWriteLock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		if (closed == true) {
			return;
		}

		alWriteLock.lock();
		try {
			super.close();
			alDeleteBuffers(h_alBuffers);
			this.closed = true;
		} finally {
			alWriteLock.unlock();
		}
	}

}
