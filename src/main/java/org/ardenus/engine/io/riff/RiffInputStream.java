package org.ardenus.engine.io.riff;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.input.RandomAccessFileInputStream;
import org.ardenus.engine.io.LittleDataInput;

/**
 * An input stream for a {@link RiffFile RIFF} container.
 * <p>
 * A {@code RiffInputStream} contains an internal {@link RandomAccessFile},
 * which it obtains from a {@code RIFF} container as its basic source of data.
 * Methods relating to reading {@code RIFF} file data such as
 * {@link #readShortLE()} and {@link #readIntLE()} are also present.
 * 
 * @see RiffChunkInputStream
 */
public class RiffInputStream extends FilterInputStream
		implements LittleDataInput {

	private final RandomAccessFile file;

	/**
	 * Creates a {@code RiffInputStream} for a {@code RandomAccessFile}.
	 *
	 * @param file
	 *            the file to read from.
	 */
	public RiffInputStream(RandomAccessFile file) {
		super(new RandomAccessFileInputStream(file));
		this.file = file;
	}

	/**
	 * Returns the current offset of the file this input stream reads from.
	 *
	 * @return the offset from the beginning of the file, in bytes, at which the
	 *         next read or write occurs.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public long ptr() throws IOException {
		return file.getFilePointer();
	}

	/**
	 * Sets the file-pointer offset, measured from the beginning of this file,
	 * at which the next read occurs.
	 *
	 * @param pos
	 *            the offset position, measured in bytes from the beginning of
	 *            the file, at which to set the file pointer.
	 * @throws IOException
	 *             if {@code pos} is negative or greater than the chunk size or
	 *             another I/O error occurs.
	 * @see RandomAccessFile#seek(long)
	 */
	public void seek(long pos) throws IOException {
		if (pos < 0) {
			throw new IOException("negative seek offset");
		}
		file.seek(pos);
	}

	@Override
	public int available() throws IOException {
		return (int) (file.length() - this.ptr());
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		file.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		file.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return file.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return file.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return file.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return file.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return file.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return file.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return file.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return file.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return file.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return file.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return file.readDouble();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public short readShortLE() throws IOException {
		return EndianUtils.readSwappedShort(this);
	}

	@Override
	public int readIntLE() throws IOException {
		return EndianUtils.readSwappedInteger(this);
	}

	@Override
	public long readLongLE() throws IOException {
		return EndianUtils.readSwappedLong(this);
	}

	@Override
	public float readFloatLE() throws IOException {
		return EndianUtils.readSwappedFloat(this);
	}

	@Override
	public double readDoubleLE() throws IOException {
		return EndianUtils.readSwappedDouble(this);
	}

	/**
	 * Closes this {@code RIFF} container's input stream. A closed {@code RIFF}
	 * input stream cannot perform input operations and cannot be reopened.
	 * Closing a {@code RIFF} container input stream is not equivalent to
	 * closing a {@code RIFF} container file.
	 */
	@Override
	public void close() throws IOException {
		super.close(); /* override JavaDoc message */
	}

}
