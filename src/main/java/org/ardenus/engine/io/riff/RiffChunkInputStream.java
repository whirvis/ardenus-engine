package org.ardenus.engine.io.riff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * A {@link RiffInputStream} which limits itself to a specific chunk of a
 * {@link RiffFile RIFF} container.
 * <p>
 * The purpose of this specialized input stream is to prevent accidental
 * reading, seeking, etc. outside of a chunk. Trying to do so will resut in an
 * {@code IOException} being thrown.
 * 
 * @see RiffFile#openChunk(String)
 */
public class RiffChunkInputStream extends RiffInputStream {

	private final RiffChunkHeader header;

	/**
	 * Creates a {@code RiffChunkInputStream} for a {@link RiffFile RIFF} chunk.
	 * The chunk must be sought to before the construction of this input stream.
	 *
	 * @param file
	 *            the file to read from.
	 * @param header
	 *            the header of the {@code RIFF} chunk.
	 * @throws NullPointerException
	 *             if {@code file} or {@code header} are {@code null}.
	 * @throws RiffException
	 *             if {@code this.ptr()} does not point to {@code header.ptr}.
	 * @throws IOException
	 *             if an I/O error occurrs.
	 */
	public RiffChunkInputStream(RandomAccessFile file, RiffChunkHeader header)
			throws IOException {
		super(file);
		this.header = Objects.requireNonNull(header, "header");
		if (this.ptr() != header.ptr) {
			throw new RiffException("must seek to chunk");
		}
	}

	/**
	 * Returns the header of the chunk this stream is reading from.
	 * 
	 * @return the chunk header, guaranteed not to be {@code null}.
	 */
	public RiffChunkHeader getHeader() {
		return this.header;
	}

	/**
	 * Returns the file pointer at which the chunk ends.
	 * 
	 * @return the file pointer at which the chunk ends.
	 */
	private long getEndPtr() {
		return header.ptr + header.size;
	}

	/**
	 * Sets the chunk-pointer offset, measured from the beginning of this chunk,
	 * at which the next read occurs.
	 *
	 * @param chunkPos
	 *            the offset position, measured in bytes from the beginning of
	 *            the chunk, at which to set the file pointer.
	 * @throws IOException
	 *             if {@code chunkPos} is negative or an I/O error occurs.
	 * @see RandomAccessFile#seek(long)
	 */
	@Override
	public void seek(long chunkPos) throws IOException {
		if (chunkPos < 0) {
			throw new IOException("negative chunkPos");
		}
		super.seek(header.ptr + chunkPos);
	}

	@Override
	public int available() throws IOException {
		return (int) (this.getEndPtr() - this.ptr());
	}

	@Override
	public int read() throws IOException {
		if (this.ptr() >= this.getEndPtr()) {
			return -1; /* end of this chunk */
		}
		return super.read();
	}

	/**
	 * Closes this chunk's input stream. A closed chunk input stream cannot
	 * perform input operations and cannot be reopened.
	 */
	@Override
	public void close() throws IOException {
		super.close(); /* override JavaDoc message */
	}

}
