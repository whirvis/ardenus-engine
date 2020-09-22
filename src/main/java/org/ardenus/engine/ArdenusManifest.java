package org.ardenus.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * The reader for the Ardenus Engine manifest file.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public class ArdenusManifest extends Manifest {

	public static final Name MAIN = new Name("Main");
	private static final Name[] REQUIRED = new Name[] { MAIN };

	/**
	 * Loads an Ardenus Engine manifest from an input stream.
	 * 
	 * @param in
	 *            the input stream.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ArdenusManifest(InputStream in) throws IOException {
		super(in);
	}

}
