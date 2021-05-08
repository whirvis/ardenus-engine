package org.ardenus.engine.audio.vorbis;

import static org.lwjgl.stb.STBVorbis.*;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

/**
 * Contains stream information about an {@link VorbisFile OGG} file.
 */
public class VorbisInfo {

	/**
	 * Constructs a new {@link VorbisInfo} by getting the vorbis info from a
	 * {@code OGG} vorbis decoder.
	 * 
	 * @param h_stbVorbisDecoder
	 *            the {@code OGG} vorbis decoder.
	 * @return the {@code OGG} stream info container.
	 */
	public static VorbisInfo get(long h_stbVorbisDecoder) {
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
