package org.ardenus.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * The reader for the Ardenus Engine manifest file.
 * 
 * @author Trent Summerlin
 * @since Ardenus Engine v0.0.1-SNAPSHOT
 */
public class ArdenusManifest extends Manifest {

	public static final String PATH = "META-INF/ARDENUS.MF";

	public static final Name MAIN = new Name("Main");
	public static final Name TITLE = new Name("Title");

	/**
	 * Loads an Ardenus Engine manifest from an <code>InputStream</code>.
	 * 
	 * @param in
	 *            the input stream.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ArdenusManifest(InputStream in) throws IOException {
		super(in);
		this.requireMainAttributes(MAIN, TITLE);
	}

	/**
	 * Loads an Ardenus Engine manifest from a <code>ClassLoader</code>.
	 * <p>
	 * This constructor is a shorthand for calling
	 * {@link #ArdenusManifest(InputStream)} with the parameter being the the
	 * resource at path {@value #PATH} grabbed and its stream opened.
	 * 
	 * @param loader
	 *            the class loader.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ArdenusManifest(ClassLoader loader) throws IOException {
		this(loader.getResource(PATH).openStream());
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

	/**
	 * Initializes and returns the main class of a game.
	 * 
	 * @param loader
	 *            the game's class loader.
	 * @return the main class.
	 * @throws NullPointerException
	 *             if <code>loader</code> is <code>null</code>.
	 * @throws ClassNotFoundException
	 *             if the main class, as specified by the {@link #MAIN}
	 *             attribute within the manifest, could not be found.
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Game> getMain(ClassLoader loader)
			throws ClassNotFoundException {
		Objects.requireNonNull(loader, "loader cannot be null");
		return (Class<? extends Game>) Class
				.forName(this.getMainAttribute(MAIN), true, loader);
	}

	/**
	 * Returns the game title.
	 * 
	 * @return the game title.
	 */
	public String getTitle() {
		return this.getMainAttribute(TITLE);
	}

}
