package org.ardenus.engine.io.riff;

import java.io.IOException;

/**
 * Describes a chunk within a {@link RiffFile RIFF} file.
 */
public class RiffChunkHeader {

	private static final int CHUNK_ID_LEN = 4;

	/**
	 * Reads a chunk header from the input stream of a {@code RIFF} container
	 * and constructs a new {@code RiffChunkHeader} from the information.
	 * 
	 * @param in
	 *            the {@code RIFF} container's input stream.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static RiffChunkHeader read(RiffInputStream in) throws IOException {
		byte[] id = new byte[CHUNK_ID_LEN];
		in.read(id);
		int size = in.readIntLE();
		long ptr = in.ptr();
		return new RiffChunkHeader(id, size, ptr);
	}

	public final String id;
	public final int size;
	public final long ptr;

	public RiffChunkHeader(String id, int size, long ptr) {
		this.id = id;
		this.size = size;
		this.ptr = ptr;
	}

	public RiffChunkHeader(byte[] id, int size, long ptr) {
		this(new String(id), size, ptr);
	}

}
