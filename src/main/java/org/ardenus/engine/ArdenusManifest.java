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
	
	public static final String PATH = "/META-INF/ARDENUS.MF";

	public static final Name MAIN = new Name("Main");

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
		this.requireMainAttributes(MAIN);
	}

	/**
	 * Ensures that a set of main attributes are present within this manifest.
	 * If any are missing, an <code>IOException</code> will be thrown.
	 * 
	 * @param names
	 *            the names of the main attributes.
	 * @throws IOException
	 *             if a main attribute under one of the names of
	 *             <code>names</code> is not present within this manifest.
	 */
	private void requireMainAttributes(Name... names) throws IOException {
		if (names != null) {
			for (Name name : names) {
				if (name != null && !this.hasMainAttribute(name)) {
					throw new IOException("missing required main attribute "
							+ name.toString());
				}
			}
		}
	}

	/**
	 * Checks and returns if a main attribute with a given name exists.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return <code>true</code> if an attribute under <code>name</code> exists,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasMainAttribute(Name name) {
		return this.getMainAttributes().get(name) != null;
	}

	/**
	 * Returns the value of a main attribute with the given name.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return the value of the attribute, trimmed by {@link String#trim()},
	 *         <code>null</code> if it does not exist.
	 */
	public String getMainAttribute(Name name) {
		String value = (String) this.getMainAttributes().get(name);
		return value != null ? value.trim() : null;
	}

	/**
	 * Returns the value of a main attribute with the given name.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return the value of the attribute, trimmed by {@link String#trim()},
	 *         <code>fallback</code> (untrimmed) if it does not exist.
	 */
	public String getMainAttribute(Name name, String fallback) {
		String value = this.getMainAttribute(name);
		return value != null ? value : fallback;
	}

}
