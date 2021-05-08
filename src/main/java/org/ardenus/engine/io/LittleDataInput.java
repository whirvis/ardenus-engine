package org.ardenus.engine.io;

import java.io.DataInput;
import java.io.IOException;

/**
 * An extension of {@link DataInput} which requires little-endian versions of
 * its read methods to be present alongside their big-endian counterparts.
 */
public interface LittleDataInput extends DataInput {

	/**
	 * A little-endian version of {@link #readShort()}.
	 * 
	 * @return the value just read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public short readShortLE() throws IOException;

	/**
	 * A little-endian version of {@link #readInt()}.
	 * 
	 * @return the value just read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int readIntLE() throws IOException;

	/**
	 * A little-endian version of {@link #readLong()}.
	 * 
	 * @return the value just read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public long readLongLE() throws IOException;

	/**
	 * A little-endian version of {@link #readFloat()}.
	 * 
	 * @return the value just read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public float readFloatLE() throws IOException;

	/**
	 * A little-endian version of {@link #readDouble()}.
	 * 
	 * @return the value just read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public double readDoubleLE() throws IOException;

}
