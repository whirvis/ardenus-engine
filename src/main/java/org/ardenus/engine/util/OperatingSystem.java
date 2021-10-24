package org.ardenus.engine.util;

public enum OperatingSystem {

	WINDOWS("windows", "Windows", false, "win"),
	OSX("osx", "Mac OSX", true, "mac", "darwin"),
	LINUX("linux", "Linux", true, "linux", "ubuntu"),
	SOLARIS("solaris", "Solaris", true, "solaris", "sun"),

	ANDROID("android", "Android", true),
	IOS("ios", "IOS", true),

	/**
	 * If this value is ever returned, please contact the developers of the
	 * engine and specify the operating system.
	 */
	UNKNOWN(null, null, false);

	public final String id;
	public final String name;
	public final boolean isUnix;
	private final String[] identifiers;

	private OperatingSystem(String id, String name, boolean isUnix,
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

	private static OperatingSystem currentOs;

	/**
	 * @return the operating system this machine is running on,
	 *         {@link OperatingSystem#UNKNOWN} if it could not be determined.
	 */
	public static OperatingSystem get() {
		if (currentOs != null) {
			return currentOs;
		}

		currentOs = OperatingSystem.UNKNOWN;
		String osName = System.getProperty("os.name");
		for (OperatingSystem os : OperatingSystem.values()) {
			if (os.isSystem(osName)) {
				currentOs = os;
				break;
			}
		}
		return currentOs;
	}

}
