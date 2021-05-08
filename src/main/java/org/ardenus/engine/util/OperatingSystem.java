package org.ardenus.engine.util;

/**
 * Represents an operating system.
 */
public enum OperatingSystem {

	WINDOWS(0x00, "Windows", false, "win"),
	MACOSX(0x01, "Mac OSX", true, "mac", "darwin"),
	LINUX(0x02, "Linux", true, "linux"),
	UBUNTU(0x03, "Ubuntu", true, "unbuntu"),
	SOLARIS(0x04, "Solaris", true, "solaris", "sun"),

	/**
	 * An unknown operating system.
	 * <p>
	 * If this value is ever returned, please contact the developers of the
	 * engine and specify the operating system.
	 */
	UNKNOWN(0xFF, null, false);

	public final int id;
	public final String name;
	public final boolean isUnix;
	private final String[] identifiers;

	private OperatingSystem(int id, String name, boolean isUnix,
			String... identifiers) {
		this.id = id;
		this.name = name;
		this.isUnix = isUnix;
		this.identifiers = identifiers;
	}

	/**
	 * Returns if a system name signifies it is this operating system.
	 * 
	 * @param os_name
	 *            the name of the system.
	 * @return {@code true} if {@code systemName} signifies it is this kind of
	 *         operating system, {@code false} otherwise.
	 */
	public boolean isSystem(String os_name) {
		if (os_name == null) {
			return false;
		}

		os_name = os_name.toLowerCase();
		for (String identifier : identifiers) {
			if (os_name.contains(identifier)) {
				return true;
			}
		}
		return false;
	}

}
