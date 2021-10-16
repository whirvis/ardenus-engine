package org.ardenus.engine.audio.vorbis;

import static org.lwjgl.stb.STBVorbis.*;

import java.util.Objects;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

/**
 * Contains stream information about an {@link VorbisFile OGG} file.
 */
public class VorbisInfo {

	/**
	 * Constructs a new {@link VorbisInfo} by getting the info from the
	 * {@code OGG} Vorbis decoder of a {@link VorbisFile}.
	 * 
	 * @param vorbis
	 *            the {@code OGG} Vorbis file.
	 * @return the {@code OGG} stream info container.
	 * @throws NullPointerException
	 *             if {@code vorbis} is {@code null}.
	 * @throws VorbisException
	 *             if {@code vorbis} has no decoder or is closed.
	 */
	public static VorbisInfo get(VorbisFile vorbis) throws VorbisException {
		/*
		 * This method originally took in the handle of the stb_vorbis_info
		 * struct. However, passing in any invalid handle results in the JVM
		 * crashing unrecoverably with an EXCEPTION_ACCESS_VIOLATION. Entrusting
		 * the VorbisFile class to generate this structure beforehand reduces
		 * the possibility of the JVM being crashed through this method.
		 */
		Objects.requireNonNull(vorbis, "vorbis");
		long h_stbVorbisDecoder = vorbis.getDecoderHandle();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			STBVorbisInfo p_vorbisInfo = STBVorbisInfo.mallocStack();
			stb_vorbis_get_info(h_stbVorbisDecoder, p_vorbisInfo);
			return new VorbisInfo(p_vorbisInfo.sample_rate(),
					p_vorbisInfo.channels(),
					p_vorbisInfo.setup_memory_required(),
					p_vorbisInfo.setup_temp_memory_required(),
					p_vorbisInfo.temp_memory_required(),
					p_vorbisInfo.max_frame_size());
		}
	}

	public final int sampleRate;
	public final int channels;
	public final int setupMemoryRequired;
	public final int setupTempMemoryRequired;
	public final int tempMemoryRequired;
	public final int maxFrameSize;

	public VorbisInfo(int sampleRate, int channels, int setupMemoryRequired,
			int setupTempMemoryRequired, int tempMemoryRequired,
			int maxFrameSize) {
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.setupMemoryRequired = setupMemoryRequired;
		this.setupTempMemoryRequired = setupTempMemoryRequired;
		this.tempMemoryRequired = tempMemoryRequired;
		this.maxFrameSize = maxFrameSize;
	}

}
