package org.ardenus.engine.audio.sound;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardenus.engine.audio.Audio;
import org.ardenus.engine.audio.AudioSource;
import org.ardenus.engine.audio.VolumeChannel;
import org.ardenus.engine.audio.sound.event.SoundPauseEvent;
import org.ardenus.engine.audio.sound.event.SoundPlayEvent;
import org.ardenus.engine.audio.sound.event.SoundStopEvent;
import org.ardenus.engine.util.fade.Fade;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * A playable sound.
 * <p>
 * No sound is playable without an implementation that specifies how its data
 * should be loaded and played. The two built-in implementations are
 * {@link BufferedSound} and {@link StreamedSound}. Proper usage of these
 * implementations usually depends on what they will be playing.
 */
public abstract class Sound implements Closeable {

	protected final AudioSource audio;
	protected final int h_alSource;
	private final Lock pvalLock;
	private final float[][] pval;

	private float volume;
	private VolumeChannel volumeChannel;
	private Fade volumeFade;
	private boolean stopAfterFade;
	private long fadeStartTimeMillis;

	private final Set<SoundTrigger> triggers;
	private final Lock updateLock;
	private boolean closed;

	/**
	 * Generates an OpenAL source to manipulate.
	 * 
	 * @param audio
	 *            the audio source to read from.
	 * @param maintain
	 *            if the sound should call {@link Audio#maintain(Sound)} at the
	 *            end of construction. This should be {@code false} for any
	 *            implementations of {@code Sound} which have more to initialize
	 *            before {@link #update()} can be called.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 * @throws SoundException
	 *             if the OpenAL source fails to generate.
	 */
	protected Sound(AudioSource audio, boolean maintain) {
		this.audio = Objects.requireNonNull(audio, "audio");
		this.h_alSource = alGenSources();
		if (h_alSource == AL_NONE) {
			throw new SoundException("failed to generate OpenAL source");
		}
		this.pvalLock = new ReentrantLock();
		this.pval = new float[3][1];

		this.volume = 1.0F;
		this.triggers = new HashSet<>();
		this.updateLock = new ReentrantLock();

		/*
		 * Only maintain at the end of construction when specified. This is
		 * because an extending class may require some additional setup before
		 * the first call to update() is made. In this case however, they are
		 * expected to notify the audio system of required maintenance.
		 */
		if (maintain == true) {
			Audio.maintain(this);
		}
	}

	/**
	 * Generates an OpenAL source to manipulate.
	 * <p>
	 * This constructor is a shorthand for {@link #Sound(AudioSource, boolean)},
	 * with the argument for {@code maintain} being {@code true}.
	 * 
	 * @param audio
	 *            the audio source to read from.
	 * @throws NullPointerException
	 *             if {@code audio} is {@code null}.
	 * @throws SoundException
	 *             if the OpenAL source fails to generate.
	 */
	public Sound(AudioSource audio) {
		this(audio, true);
	}

	/**
	 * @param alParam
	 *            the OpenAL source parameter.
	 * @return the value of {@code alParam}.
	 */
	protected int getSourcei(int alParam) {
		this.requireOpen();
		return alGetSourcei(h_alSource, alParam);
	}

	/**
	 * @param alParam
	 *            the OpenAL source parameter.
	 * @param value
	 *            the parameter value.
	 */
	protected void setSourcei(int alParam, int value) {
		this.requireOpen();
		alSourcei(h_alSource, alParam, value);
	}

	/**
	 * @param alParam
	 *            the OpenAL source parameter.
	 * @return the value of {@code alParam}.
	 */
	protected float getSourcef(int alParam) {
		this.requireOpen();
		return alGetSourcef(h_alSource, alParam);
	}

	/**
	 * @param alParam
	 *            the OpenAL source parameter.
	 * @param value
	 *            the parameter value.
	 */
	protected void setSourcef(int alParam, float value) {
		this.requireOpen();
		alSourcef(h_alSource, alParam, value);
	}

	/**
	 * Fetches a 3-dimensional OpenAL source parameter and stores its component
	 * values into the specified {@code Vector3f}.
	 * <p>
	 * This method exists purely for optimization. Since Java has no references,
	 * LWJGL makes use of arrays to hack in references. However, this can
	 * cumbersome and result in wasted resources. The use of a cached
	 * {@code pval} array to store these values before storing them inside a
	 * destination {@code Vector3f} acts as a workaround to this problem.
	 * 
	 * @param alParam
	 *            then OpenAL source parameter.
	 * @param dest
	 *            where to store the components.
	 * @return {@code dest} now storing the components of {@code alParam}.
	 * @throws NullPointerException
	 *             if {@code dest} is {@code null}.
	 * @throws IllegalStateException
	 *             if this sound is closed.
	 */
	protected Vector3f getSource3f(int alParam, Vector3f dest) {
		Objects.requireNonNull(dest, "dest");
		this.requireOpen();

		pvalLock.lock();
		try {
			alGetSource3f(h_alSource, alParam, pval[0], pval[1], pval[2]);
			dest.x = pval[0][0];
			dest.y = pval[1][0];
			dest.z = pval[2][0];
			return dest;
		} finally {
			pvalLock.unlock();
		}
	}

	/**
	 * @param alParam
	 *            the OpenAL source parameter.
	 * @param x
	 *            the X-axis component of the value.
	 * @param y
	 *            the Y-axis component of the value.
	 * @param z
	 *            the Z-axis component of the value.
	 */
	protected void setSource3f(int alParam, float x, float y, float z) {
		this.requireOpen();
		alSource3f(h_alSource, alParam, x, y, z);
	}

	/**
	 * Fetches a 2-dimensional OpenAL source parameter and stores its X-axis and
	 * Y-axis component values into the specified {@code Vector2f}.
	 * <p>
	 * This method exists purely for optimization. Since Java has no references,
	 * LWJGL makes use of arrays to hack in references. However, this can
	 * cumbersome and result in wasted resources. The use of a cached
	 * {@code pval} array to store these values before storing them inside a
	 * destination {@code Vector2f} acts as a workaround to this problem.
	 * 
	 * @param alParam
	 *            then OpenAL source parameter.
	 * @param dest
	 *            where to store the components.
	 * @return {@code dest} now storing the components of {@code alParam}.
	 * @throws NullPointerException
	 *             if {@code dest} is {@code null}.
	 * @throws IllegalStateException
	 *             if this sound is closed.
	 */
	protected Vector2f getSource2f(int alParam, Vector2f dest) {
		Objects.requireNonNull(dest, "dest");
		this.requireOpen();

		pvalLock.lock();
		try {
			alGetSource3f(h_alSource, alParam, pval[0], pval[1], pval[2]);
			dest.x = pval[0][0];
			dest.y = pval[1][0];
			return dest;
		} finally {
			pvalLock.unlock();
		}
	}

	/**
	 * @return {@code true} if this sound is playing, {@code false} otherwise.
	 */
	public boolean isPlaying() {
		int alState = this.getSourcei(AL_SOURCE_STATE);
		return alState == AL_PLAYING;
	}

	public void play() {
		this.requireOpen();
		alSourcePlay(h_alSource);
		Audio.sendEvent(new SoundPlayEvent(this));
	}

	/**
	 * @return {@code true} if the sound is paused, {@code false} otherwise.
	 */
	public boolean isPaused() {
		int alState = this.getSourcei(AL_SOURCE_STATE);
		return alState == AL_PAUSED;
	}

	/**
	 * Pausing a sound is not the same as stopping a sound. When a sound is
	 * paused, it will continue from where it was when resumed via
	 * {@link #play()}.
	 * 
	 * @see #stop()
	 */
	public void pause() {
		this.requireOpen();
		alSourcePause(h_alSource);
		Audio.sendEvent(new SoundPauseEvent(this));
	}

	/**
	 * @return {@code true} if this sound is stopped, {@code false} otherwise.
	 */
	public boolean isStopped() {
		int alState = this.getSourcei(AL_SOURCE_STATE);
		return alState == AL_STOPPED;
	}

	/**
	 * Stopping a sound is not the same as pausing a sound. When a sound is
	 * stopped, it will start from the beginning when it is started again via
	 * {@link #play()}.
	 * <p>
	 * <b>Note:</b> If the sound is currently fading to another volume, the fade
	 * will be prematurely finished and the volume set to its intended finishing
	 * value. This prevents seemingly random fades when the sound is started
	 * again.
	 * 
	 * @see #pause()
	 */
	public void stop() {
		this.requireOpen();
		if (this.isFading()) {
			this.setVolume((float) volumeFade.finish);
			this.volumeFade = null;
		}
		alSourceStop(h_alSource);
		Audio.sendEvent(new SoundStopEvent(this));
	}

	/**
	 * @return {@code true} if this sound is looping, {@code false} otherwise.
	 */
	public boolean isLooping() {
		int alLooping = this.getSourcei(AL_LOOPING);
		return alLooping == AL_TRUE;
	}

	/**
	 * @param looping
	 *            {@code true} if this sound should be looping, {@code false}
	 *            otherwise.
	 */
	public void setLooping(boolean looping) {
		int alLooping = looping ? AL_TRUE : AL_FALSE;
		this.setSourcei(AL_LOOPING, alLooping);
	}

	/**
	 * @return the sound gain.
	 */
	public float getGain() {
		return this.getSourcef(AL_GAIN);
	}

	private void updateGain() {
		float gain = this.volume;
		if (volumeChannel != null) {
			gain *= volumeChannel.getVolume();
		}
		this.setSourcef(AL_GAIN, gain);
	}

	/**
	 * @return the pitch of the sound, guaranteed to be no less than
	 *         {@code 0.0F}.
	 */
	public float getPitch() {
		return this.getSourcef(AL_PITCH);
	}

	/**
	 * @param pitch
	 *            the pitch. If less than {@code 0.0F}, the pitch will be capped
	 *            to a value of {@code 0.0F}.
	 */
	public void setPitch(float pitch) {
		this.setSourcef(AL_PITCH, Math.max(pitch, 0.0F));
	}

	/**
	 * @param pos
	 *            the {@code Vector3f} to store the position into.
	 * @return {@code pos}, now storing the position.
	 */
	public Vector3f getPosition(Vector3f pos) {
		return this.getSource3f(AL_POSITION, pos);
	}

	/**
	 * @param pos
	 *            the {@code Vector2f} to store the position into.
	 * @return {@code pos}, now storing the position.
	 */
	public Vector2f getPosition(Vector2f pos) {
		return this.getSource2f(AL_POSITION, pos);
	}

	/**
	 * @param x
	 *            the X-axis position.
	 * @param y
	 *            the Y-axis position.
	 * @param z
	 *            the Z-axis position.
	 */
	public void setPosition(float x, float y, float z) {
		this.setSource3f(AL_POSITION, x, y, z);
	}

	/**
	 * This method is a shorthand for {@link #setPosition(float, float, float)},
	 * with the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis position.
	 * @param y
	 *            the Y-axis position.
	 */
	public void setPosition(float x, float y) {
		this.setPosition(x, y, 0.0F);
	}

	/**
	 * This method is a shorthand for {@link #setPosition(float, float, float)},
	 * with the {@code x}, {@code y}, and {@code z} parameters being the
	 * component vectors of {@code pos}.
	 * 
	 * @param pos
	 *            the position.
	 */
	public void setPosition(Vector3f pos) {
		this.setPosition(pos.x, pos.y, pos.z);
	}

	/**
	 * This method is a shorthand for {@link #setPosition(float, float, float)},
	 * with the {@code x} and {@code y} parameters being the component vectors
	 * of {@code pos}, and the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param pos
	 *            the position.
	 */
	public void setPosition(Vector2f pos) {
		this.setPosition(pos.x, pos.y, 0.0F);
	}

	/**
	 * @param vel
	 *            the {@code Vector3f} to store the velocity into.
	 * @return {@code vel}, now storing the velocity.
	 */
	public Vector3f getVelocity(Vector3f vel) {
		return this.getSource3f(AL_VELOCITY, vel);
	}

	/**
	 * @param vel
	 *            the {@code Vector2f} to store the velocity into.
	 * @return {@code vel}, now storing the velocity.
	 */
	public Vector2f getVelocity(Vector2f vel) {
		return this.getSource2f(AL_VELOCITY, vel);
	}

	/**
	 * @param x
	 *            the X-axis velocity.
	 * @param y
	 *            the Y-axis velocity.
	 * @param z
	 *            the Z-axis velocity.
	 */
	public void setVelocity(float x, float y, float z) {
		this.setSource3f(AL_VELOCITY, x, y, z);
	}

	/**
	 * This method is a shorthand for {@link #setVelocity(float, float, float)},
	 * with the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis velocity.
	 * @param y
	 *            the Y-axis velocity.
	 */
	public void setVelocity(float x, float y) {
		this.setVelocity(x, y, 0.0F);
	}

	/**
	 * This method is a shorthand for {@link #setVelocity(float, float, float)},
	 * with the {@code x}, {@code y}, and {@code z} parameters being the
	 * component vectors of {@code vel}.
	 * 
	 * @param vel
	 *            the velocity.
	 */
	public void setVelocity(Vector3f vel) {
		this.setVelocity(vel.x, vel.y, vel.z);
	}

	/**
	 * This method is a shorthand for {@link #setVelocity(float, float, float)},
	 * with the {@code x} and {@code y} parameters being the component vectors
	 * of {@code vel}, and the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param vel
	 *            the velocity.
	 */
	public void setVelocity(Vector2f vel) {
		this.setVelocity(vel.x, vel.y, 0.0F);
	}

	/**
	 * @param rot
	 *            the {@code Vector3f} to store the orientation into.
	 * @return {@code rot}, now storing the orientation.
	 */
	public Vector3f getOrientation(Vector3f rot) {
		return this.getSource3f(AL_ORIENTATION, rot);
	}

	/**
	 * @param rot
	 *            the {@code Vector2f} to store the orientation into.
	 * @return {@code rot}, now storing the orientation.
	 */
	public Vector2f getOrientation(Vector2f rot) {
		return this.getSource2f(AL_ORIENTATION, rot);
	}

	/**
	 * @param x
	 *            the X-axis orientation.
	 * @param y
	 *            the Y-axis orientation.
	 * @param z
	 *            the Z-axis orientation.
	 */
	public void setOrientation(float x, float y, float z) {
		this.setSource3f(AL_ORIENTATION, x, y, z);
	}

	/**
	 * This method is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code z}
	 * parameter being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis orientation.
	 * @param y
	 *            the Y-axis orientation.
	 */
	public void setOrientation(float x, float y) {
		this.setOrientation(x, y, 0.0F);
	}

	/**
	 * This method is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code x},
	 * {@code y}, and {@code z} parameters being the component vectors of
	 * {@code rot}.
	 * 
	 * @param rot
	 *            the orientation.
	 */
	public void setOrientation(Vector3f rot) {
		this.setOrientation(rot.x, rot.y, rot.z);
	}

	/**
	 * This method is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code x} and
	 * {@code y} parameters being the component vectors of {@code rot}, and the
	 * {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param rot
	 *            the orientation.
	 */
	public void setOrientation(Vector2f rot) {
		this.setOrientation(rot.x, rot.y, 0.0F);
	}

	/**
	 * @return the amount of bytes that have played since this sound was
	 *         started, assuming it has not been stopped.
	 */
	public int getByteOffset() {
		return this.getSourcei(AL_BYTE_OFFSET);
	}

	/**
	 * @param byteOffset
	 *            the byte offset.
	 * @throws IndexOutOfBoundsException
	 *             if {@code byteOffset} is negative.
	 */
	public void setByteOffset(int byteOffset) {
		if (byteOffset < 0) {
			throw new IndexOutOfBoundsException("byteOffset < 0");
		}
		this.setSourcei(AL_BYTE_OFFSET, byteOffset);
	}

	/**
	 * @return the amount of samples that have played since this sound was
	 *         started, assuming it has not been stopped.
	 */
	public int getSampleOffset() {
		return this.getSourcei(AL_SAMPLE_OFFSET);
	}

	/**
	 * @param sampleOffset
	 *            the sample offset.
	 * @throws IndexOutOfBoundsException
	 *             if {@code sampleOffset} is negative.
	 */
	public void setSampleOffset(int sampleOffset) {
		if (sampleOffset < 0) {
			throw new IndexOutOfBoundsException("sampleOffset < 0");
		}
		this.setSourcei(AL_SAMPLE_OFFSET, sampleOffset);
	}

	/**
	 * This method returns a {@code float} representing how many seconds have
	 * been played. If {@code wholeSeconds} is {@code false}, this value is
	 * calculated using the return value of {@link #getSampleOffset()}. This is
	 * done because the value returned by {@code AL_SEC_OFFSET} is only accurate
	 * to the whole second.
	 * 
	 * @param wholeSeconds
	 *            {@code true} if the offset should be returned in whole
	 *            seconds, {@code false} for sample precision.
	 * @return the offset of this sound in seconds.
	 */
	public float getOffset(boolean wholeSeconds) {
		if (wholeSeconds == true) {
			return this.getSourcei(AL_SEC_OFFSET);
		}
		float sampleOffset = this.getSampleOffset();
		return sampleOffset / audio.getFrequencyHz();
	}

	/**
	 * This method returns a {@code float} representing how many seconds have
	 * been played. This value is calculated using the return value of
	 * {@link #getSampleOffset()}. This is done because the value returned by
	 * {@code AL_SEC_OFFSET} is only accurate to the whole second.
	 * <p>
	 * This method is a shorthand for {@link #getOffset(boolean)}, with the
	 * {@code wholeSeconds} parameter being set to {@code false}.
	 * 
	 * @return the offset of this sound in seconds.
	 */
	public float getOffset() {
		return this.getOffset(false);
	}

	/**
	 * @param offset
	 *            the offset in seconds.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offset} is negative.
	 */
	public void setOffset(float offset) {
		if (offset < 0) {
			throw new IndexOutOfBoundsException("offset < 0");
		}
		float sampleOffset = offset * audio.getFrequencyHz();
		this.setSampleOffset(Math.round(sampleOffset));
	}

	/**
	 * This method returns a {@code long} representing how many milliseconds
	 * have been played. This value is calculated by taking the return value of
	 * {@link #getOffset()}, multiplying it by {@code 1000L}, and casting it
	 * back to a {@code long}.
	 * 
	 * @return the offset of this sound in milliseconds.
	 */
	public long getOffsetMillis() {
		double offset = this.getOffset();
		return Math.round(offset * 1000L);
	}

	/**
	 * @param offsetMillis
	 *            the offset in milliseconds.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offsetMillis} is negative.
	 */
	public void setOffsetMillis(long offsetMillis) {
		if (offsetMillis < 0) {
			throw new IndexOutOfBoundsException("offsetMillis < 0");
		}
		this.setOffset(offsetMillis / 1000.0F);
	}

	/**
	 * Whether or not this method is supported depends on {@code audio}
	 * supporting {@link AudioSource#pcmLength()}. Most audio sources reading
	 * from a file can return how many bytes of PCM there are to read. However,
	 * some cannot.
	 * 
	 * @return the length of this sound in seconds.
	 * @throws UnsupportedOperationException
	 *             if {@code audio} does not support
	 *             {@link AudioSource#pcmLength()}.
	 */
	public float getLength() {
		/*
		 * The length is determind by the sample count divided by the frequency
		 * in Hz multiplied by the channel count. The channel count is taken
		 * into account here because at playback OpenAL will play all present
		 * channels at the same time.
		 */
		float sampleCount = audio.pcmLength() / audio.getBytesPerSample();
		return sampleCount / (audio.getFrequencyHz() * audio.getChannelCount());
	}

	/**
	 * Whether or not this method is supported depends on {@code audio}
	 * supporting {@link AudioSource#pcmLength()}. Most audio sources reading
	 * from a file can return how many bytes of PCM there are to read. However,
	 * some cannot.
	 * 
	 * @return the length of this sound in milliseconds.
	 * @throws UnsupportedOperationException
	 *             if {@code audio} does not support
	 *             {@link AudioSource#pcmLength()}.
	 */
	public long getLengthMillis() {
		float length = this.getLength();
		return Math.round(length * 1000L);
	}

	/**
	 * Whether or not this method is supported depends on {@code audio}
	 * supporting {@link AudioSource#pcmLength()}. Most audio sources reading
	 * from a file can return how many bytes of PCM there are to read. However,
	 * some cannot.
	 * 
	 * @return the progression of this sound, on a scale of {@code 0.0F} to
	 *         {@code 1.0F}.
	 * @throws UnsupportedOperationException
	 *             if {@code audio} does not support
	 *             {@link AudioSource#pcmLength()}.
	 * @see #getLength()
	 * @see #getOffset()
	 */
	public float getProgression() {
		/* use bytes since less calculations required */
		float offsetBytes = this.getByteOffset();
		return offsetBytes / audio.pcmLength();
	}

	/**
	 * The return value of this method is not influenced by the volume channel
	 * this sound is registered to, if any. Furthermore, the final gain of the
	 * sound (which is not returned by this method) is influenced only when
	 * {@link #update()} is called.
	 * 
	 * @return the sound volume, guaranteed to be no less than {@code 0.0F}.
	 */
	public float getVolume() {
		return this.volume;
	}

	private void setVolume(float volume, boolean fadeUpdate) {
		if (this.isFading() && fadeUpdate == false) {
			throw new IllegalStateException("fading volume");
		}
		this.volume = Math.max(volume, 0.0F);
	}

	/**
	 * The volume determines the initial gain of the sound. If this sound is
	 * registered to a {@link VolumeChannel}, it too will influence the gain of
	 * this sound. The gain of a sound is equal to the volume multiplied by the
	 * volume of its channel (or simply {@code 1.0F} if not registered to a
	 * volume channel).
	 * 
	 * @param volume
	 *            the volume. If less than {@code 0.0F}, the volume will be
	 *            capped to a value of {@code 0.0F}.
	 * @throws IllegalStateException
	 *             if this sound is currently fading to another volume.
	 * @see #setVolumeChannel(VolumeChannel)
	 * @see #isFading()
	 */
	public void setVolume(float volume) {
		this.setVolume(volume, false);
	}

	/**
	 * @return the volume channel, may be {@code null}.
	 */
	public VolumeChannel getVolumeChannel() {
		return this.volumeChannel;
	}

	/**
	 * The volume channel determines the final gain of the sound. When the sound
	 * is registered to a volume channel, it will influence its gain. The gain
	 * of a sound is equal to the sound of the volume multiplied by the volume
	 * of the volume channel (or simply {@code 1.0F} if no volume channel).
	 * 
	 * @param volumeChannel
	 *            the volume channel, may be {@code null}.
	 * @see #setVolume(float)
	 */
	public void setVolumeChannel(VolumeChannel volumeChannel) {
		this.volumeChannel = volumeChannel;
	}

	/**
	 * @return {@code true} if this sound is currently fading its volume to
	 *         another level, {@code false} otherwise.
	 */
	public boolean isFading() {
		return volumeFade != null;
	}

	/**
	 * Calling this method while another fade is occurring will have it be
	 * cancelled out in favor of the new one. Furthermore, the fade will not
	 * begin until the sound is playing, and will not update when the sound is
	 * paused. If the sound is stopped during a fade, the fade is cancelled out.
	 * 
	 * @param fade
	 *            the fade, which will be updated automatically by the sound.
	 *            May be {@code null} to cancel the current fade, if any.
	 * @param stop
	 *            {@code true} if the sound should be stopped after {@code fade}
	 *            is complete, {@code false} otherwise.
	 * @see #setVolume(float)
	 */
	public void fade(Fade fade, boolean stop) {
		/*
		 * Use the sound offset for keeping track of fade time. If the sound has
		 * its offset changed, freezes in playback, etc., the fade will still be
		 * correct. Using a system time would result in a fade that occurs no
		 * matter how the song is manipulated during the fade time.
		 */
		this.fadeStartTimeMillis = this.getOffsetMillis();

		this.volumeFade = fade;
		this.stopAfterFade = stop;
	}

	private void updateFade() {
		if (!this.isFading() || !this.isPlaying()) {
			return;
		}

		long offset = this.getOffsetMillis() - fadeStartTimeMillis;
		float volume = (float) volumeFade.atOffset(offset);
		this.setVolume(volume, true);

		if (volume == volumeFade.finish) {
			this.volumeFade = null;
			if (stopAfterFade == true) {
				this.stopAfterFade = false;
				this.stop();
			}
		}
	}

	/**
	 * @param trigger
	 *            the trigger to add.
	 * @return {@code true} if {@code trigger} was added, {@code false}
	 *         otherwise.
	 * @throws NullPointerException
	 *             if {@code trigger} is {@code null}.
	 * @throws IllegalStateException
	 *             if this sound is playing.
	 */
	public boolean addTrigger(SoundTrigger trigger) {
		Objects.requireNonNull(trigger, "trigger");
		if (this.isPlaying()) {
			throw new IllegalStateException(
					"cannot add triggers during playback");
		}
		return triggers.add(trigger);
	}

	/**
	 * @param trigger
	 *            the trigger to remove.
	 * @return {@code true} if {@code trigger} was removed, {@code false}
	 *         otherwise.
	 * @throws IllegalStateException
	 *             if this sound is playing.
	 */
	public boolean removeTrigger(SoundTrigger trigger) {
		if (this.isPlaying()) {
			throw new IllegalStateException(
					"cannot remove triggers during playback");
		}
		return triggers.remove(trigger);
	}

	/**
	 * @param id
	 *            the ID of the triggers to remove.
	 * @return the amount of {@code SoundTrigger} instances with {@code id} that
	 *         were removed.
	 */
	public int removeTriggers(int id) {
		int removed = 0;
		Iterator<SoundTrigger> triggersI = triggers.iterator();
		while (triggersI.hasNext()) {
			if (triggersI.next().id == id) {
				triggersI.remove();
				removed++;
			}
		}
		return removed;
	}

	private void testTriggers() {
		if (!this.isPlaying()) {
			return;
		}
		long offsetMillis = this.getOffsetMillis();
		for (SoundTrigger trigger : triggers) {
			trigger.test(this, offsetMillis);
		}
	}

	/**
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void update() throws IOException {
		this.requireOpen();
		updateLock.lock();
		try {
			this.testTriggers();

			/*
			 * Update the fade before the gain, since the fade will update the
			 * volume of this sound (which the gain update depends on).
			 */
			this.updateFade();
			this.updateGain();
		} finally {
			updateLock.unlock();
		}
	}

	protected void requireOpen() {
		if (closed == true) {
			throw new IllegalStateException("sound closed");
		}
	}

	@Override
	public void close() throws IOException {
		if (closed == true) {
			return;
		}

		updateLock.lock();
		try {
			Audio.abandon(this);
			this.stop();
			alDeleteSources(h_alSource);
			this.closed = true;
		} finally {
			updateLock.unlock();
		}
	}

}
