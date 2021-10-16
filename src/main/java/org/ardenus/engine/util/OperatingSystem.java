package org.ardenus.engine.util;

public enum OperatingSystem {

	WINDOWS(0x00, "Windows", false, "win"),
	MACOSX(0x01, "Mac OSX", true, "mac", "darwin"),
	LINUX(0x02, "Linux", true, "linux"),
	UBUNTU(0x03, "Ubuntu", true, "unbuntu"),
	SOLARIS(0x04, "Solaris", true, "solaris", "sun"),

	/**
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
	 * @param osName
	 *            the name of the system.
	 * @return {@code true} if {@code systemName} signifies it is this kind of
	 *         operating system, {@code false} otherwise.
	 */
	public boolean isSystem(String osName) {
		if (osName == null) {
			return false;
		}

		osName = osName.toLowerCase();
		for (String identifier : identifiers) {
			if (osName.contains(identifier)) {
				return true;
			}
		}
		return false;
	}

}
