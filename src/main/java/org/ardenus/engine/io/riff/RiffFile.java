package org.ardenus.engine.io.riff;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A file in the {@code RIFF} container format.
 * 
 * @see RiffInputStream
 * @see RiffChunkHeader
 */
public class RiffFile implements Closeable {

	public static final byte[] SIGNATURE = { 'R', 'I', 'F', 'F' };
	public static final int FORMAT_LEN = 4;

	private final File file;
	private final RandomAccessFile access;
	private final RiffInputStream in;
	private final byte[] format;
	private final Map<String, RiffChunkHeader> chunkHeaders;
	private RiffChunkInputStream chunkIn;
	private boolean closed;

	/**
	 * Loads a {@code RIFF} container from a random access file.
	 * <p>
	 * This constructor begins reading from the file. This is to validate that
	 * {@code file} is a valid {@code RIFF} container, to check the format, and
	 * to catche all chunk headers. Only the chunk <i>headers</i> are cached,
	 * not the actual chunk data. To read the data found within a chunk, use
	 * {@link #openChunk(String)}.
	 * 
	 * @param format
	 *            the container format. A {@code null} value is permitted, and
	 *            indicates the format should not be validated.
	 * @param file
	 *            the {@code RIFF} file.
	 * @throws NullPointerException
	 *             if {@code file} is {@code null}.
	 * @throws RiffException
	 *             if {@code file} is not a valid {@code RIFF} container.
	 * @throws IOException
	 *             If an I/O error occurs while reading.
	 */
	public RiffFile(byte[] format, File file) throws IOException {
		this.file = Objects.requireNonNull(file, "file");
		this.access = new RandomAccessFile(file, "r");
		this.in = new RiffInputStream(access);

		this.validateSignature();
		int size = in.readIntLE();

		/*
		 * If no format was specified in the constructor, the user does not care
		 * what type of RIFF file this is. If one was specified, then the format
		 * must be validated immediately before object construction finishes.
		 */
		if (format == null) {
			this.format = this.readFormat();
		} else {
			this.format = format;
			this.validateFormat();
		}

		/*
		 * Cache the chunk headers. This will allow the file to be skipped
		 * around later without a need for expensive reads to "rediscover"
		 * chunks in the file when they are requested.
		 */
		this.chunkHeaders = new HashMap<>();
		while (in.ptr() < size) {
			RiffChunkHeader header = RiffChunkHeader.read(in);
			if (in.skip(header.size) < header.size) {
				throw new RiffException("chunk larger than remaining file");
			}
			chunkHeaders.put(header.id, header);
		}
	}

	/**
	 * Loads a {@code RIFF} container from a random access file.
	 * <p>
	 * This constructor begins reading from the file. This is to validate that
	 * {@code file} is a valid {@code RIFF} container, to check the format, and
	 * to catche all chunk headers. Only the chunk <i>headers</i> are cached,
	 * not the actual chunk data. To read the data found within a chunk, use
	 * {@link #openChunk(String)}.
	 * 
	 * @param format
	 *            the container format. A {@code null} value is permitted, and
	 *            indicates the format should not be validated.
	 * @param file
	 *            the {@code RIFF} file.
	 * @throws NullPointerException
	 *             if {@code file} is {@code null}.
	 * @throws RiffException
	 *             if {@code file} is not a valid {@code RIFF} container.
	 * @throws IOException
	 *             If an I/O error occurs while reading.
	 */
	public RiffFile(String format, File file) throws IOException {
		this(format.getBytes(), file);
	}

	/**
	 * Loads a {@code RIFF} container from a random access file.
	 * <p>
	 * This constructor begins reading from the file. This is to validate that
	 * {@code file} is a valid {@code RIFF} container, read the format, and to
	 * catche all chunk headers. Only the chunk <i>headers</i> are cached, not
	 * the actual chunk data. To read the data found within a chunk, use
	 * {@link #openChunk(String)}.
	 * 
	 * @param file
	 *            the {@code RIFF} file.
	 * @throws NullPointerException
	 *             if {@code file} is {@code null}.
	 * @throws RiffException
	 *             if {@code file} is not a valid {@code RIFF} container.
	 * @throws IOException
	 *             If an I/O error occurs while reading.
	 */
	public RiffFile(File file) throws IOException {
		this((byte[]) null, file);
	}

	/**
	 * Returns the file of this {@code RIFF} container.
	 * 
	 * @return the file.
	 */
	public File getFile() {
		return this.file;
	}

	private void validateSignature() throws IOException {
		byte[] sig = new byte[SIGNATURE.length];
		in.read(sig);
		if (!Arrays.equals(sig, SIGNATURE)) {
			throw new RiffException("not a RIFF file");
		}
	}

	private byte[] readFormat() throws IOException {
		byte[] format = new byte[FORMAT_LEN];
		in.read(format);
		return format;
	}

	private void validateFormat() throws IOException {
		byte[] format = this.readFormat();
		if (!Arrays.equals(format, this.format)) {
			throw new RiffException("unexpected format");
		}
	}

	/**
	 * Returns the format of this container.
	 * 
	 * @return the format ID.
	 */
	public byte[] getRiffFormat() {
		return this.format;
	}

	/**
	 * Returns if this {@code RIFF} file has a chunk with the specified ID.
	 * 
	 * @param id
	 *            the chunk ID.
	 * @return if a chunk with {@code id} is present, {@code false} otherwise.
	 */
	public boolean hasChunk(String id) {
		return chunkHeaders.containsKey(id);
	}

	/**
	 * Returns the IDs of all chunks present in this {@code RIFF} container.
	 * 
	 * @return the chunk IDs.
	 */
	public Set<String> getChunkIds() {
		return Collections.unmodifiableSet(chunkHeaders.keySet());
	}

	/**
	 * Returns a chunk's header information by its ID.
	 * 
	 * @param id
	 *            the chunk ID.
	 * @return a {@code RiffChunkHeader} for chunk with {@code id}, {@code null}
	 *         if none exists.
	 */
	public RiffChunkHeader getChunkHeader(String id) {
		return chunkHeaders.get(id);
	}

	/**
	 * Opens a new {@link RiffChunkInputStream} for a chunk of this {@code RIFF}
	 * container by its ID.
	 * <p>
	 * Because the internal {@code RandomAccessFile} can only point to one chunk
	 * at a time, only one chunk can be open at once. If a chunk been opened
	 * previously, it will be closed automatically by the invocation of this
	 * function.
	 * 
	 * @param id
	 *            the chunk ID.
	 * @return a new {@code RiffChunkInputStream} to read the chunk.
	 * @throws IOException
	 *             if no chunk by {@code id} exists or another I/O error
	 *             occurrs.
	 */
	public RiffChunkInputStream openChunk(String id) throws IOException {
		if (closed == true) {
			throw new RiffException("closed container");
		} else if (chunkIn != null) {
			chunkIn.close();
		}

		RiffChunkHeader header = chunkHeaders.get(id);
		if (header == null) {
			throw new RiffException("no such chunk");
		}

		in.seek(header.ptr);
		this.chunkIn = new RiffChunkInputStream(access, header);
		return this.chunkIn;
	}

	/**
	 * Closes this {@code RIFF} container file. A closed container cannot open
	 * any input streams for its chunks and cannot be reopened.
	 */
	@Override
	public void close() throws IOException {
		if (chunkIn != null) {
			chunkIn.close();
		}
		access.close();
		this.closed = true;
	}

}
