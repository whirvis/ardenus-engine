package org.ardenus.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * The manifest class for games written in the Ardenus engine. It is used to
 * specify game information such as name, verison, etc. as well as how they
 * should be run.
 */
public class ArdenusManifest extends Manifest {

	public static final String PATH = "META-INF/ARDENUS.MF";

	public static final Name MAIN = new Name("Main");

	private static InputStream open(ClassLoader loader) throws IOException {
		Objects.requireNonNull(loader, "loader");
		URL url = loader.getResource(PATH);
		if (url == null) {
			throw new FileNotFoundException("classpath missing " + PATH);
		}
		return url.openStream();
	}

	/**
	 * Constructs a new {@code ArdenusManifest} and loads it from an
	 * {@code InputStream}.
	 * 
	 * @param in
	 *            the input stream.
	 * @throws NullPointerException
	 *             if {@code in} is {@code null}.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ArdenusManifest(InputStream in) throws IOException {
		super(Objects.requireNonNull(in, "in"));
		this.requireMainAttributes(MAIN);
	}

	/**
	 * Loads an Ardenus Engine manifest from a <code>ClassLoader</code>.
	 * <p>
	 * This constructor is a shorthand for
	 * {@link #ArdenusManifest(InputStream)}, with the {@code in} parameter
	 * being set to {@code loader.getResource(PATH).openStream()}.
	 * 
	 * @param loader
	 *            the class loader.
	 * @throws NullPointerException
	 *             if {@code loader} is {@code null}.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ArdenusManifest(ClassLoader loader) throws IOException {
		this(open(loader));
	}

	/**
	 * Requires that a set of main attributes are present within this
	 * manifest.<br>
	 * If any are missing, an {@code IOException} will be thrown.
	 * 
	 * @param names
	 *            the names of the required main attributes.
	 * @throws NullPointerException
	 *             if {@code names} or one of its elements are {@code null}.
	 * @throws IOException
	 *             if one of the specified required main attributes is absent
	 *             from this manifest.
	 */
	private void requireMainAttributes(Name... names) throws IOException {
		Objects.requireNonNull(names, "names");
		for (Name name : names) {
			Objects.requireNonNull(name, "name");
			if (!this.hasMainAttribute(name)) {
				throw new IOException("missing " + name.toString());
			}
		}
	}

	/**
	 * Returns if a main attribute exists.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return {@code true} if an attribute under {@code name} exists,
	 *         {@code false} otherwise.
	 */
	public boolean hasMainAttribute(Name name) {
		if (name == null) {
			return false;
		}
		return this.getMainAttributes().get(name) != null;
	}

	/**
	 * Returns the value of a main attribute.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return the value of the attribute trimmed by {@link String#trim()},
	 *         {@code null} if it does not exist.
	 */
	public String getMainAttribute(Name name) {
		if (name == null) {
			return null;
		}
		String value = (String) this.getMainAttributes().get(name);
		return value != null ? value.trim() : null;
	}

	/**
	 * Returns the value of a main attribute.
	 * 
	 * @param name
	 *            the attribute name.
	 * @param fallback
	 *            the value to fallback to if no such attribute exists.
	 * @return the value of the attribute trimmed by {@link String#trim()},
	 *         {@code fallback} (untrimmed) if it does not exist.
	 */
	public String getMainAttribute(Name name, String fallback) {
		String value = this.getMainAttribute(name);
		return value != null ? value : fallback;
	}

	/**
	 * Initializes and returns the {@code Game} class.
	 * 
	 * @param loader
	 *            the game's class loader.
	 * @return the main class.
	 * @throws NullPointerException
	 *             if {@code loader} is {@code null}.
	 * @throws ClassNotFoundException
	 *             if the main class, as specified by the {@link #MAIN}
	 *             attribute within the manifest, could not be found.
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Game> getMain(ClassLoader loader)
			throws ClassNotFoundException {
		Objects.requireNonNull(loader, "loader");
		String gameClazzName = this.getMainAttribute(MAIN);
		Class<?> gameClazz = Class.forName(gameClazzName, true, loader);
		return (Class<? extends Game>) gameClazz;
	}

}
