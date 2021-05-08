package org.ardenus.engine.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ardenus.engine.Ardenus;

/**
 * Utilities for retrieval of system information and process management.
 */
public final class Program {

	private Program() {
		/* static class */
	}
	
	/**
	 * A hack that lowers the program usage of a Java program on the CPU.
	 * <p>
	 * This is most useful in infinite loops that have no inherit need for
	 * sleeps. The hack works by sleeping the current thread by one nanosecond.
	 * If an {@code InterruptedException} is thrown during the sleep,
	 * {@code thread} is sent an interrupt signal.
	 * 
	 * @param thread
	 *            the thread whose usage is being lowered.
	 */
	public static void lowerUsage(Thread thread) {
		try {
			/* lower CPU usage hack */
			Thread.sleep(0, 1);
		} catch (InterruptedException e) {
			thread.interrupt();
		}
	}

	private static String restart0() {
		File binPath = new File(
				System.getProperty("java.home") + File.separator + "bin");
		if (!binPath.exists()) {
			return "Java binary could not be located";
		}

		File runningJar = getRunningFile();
		if (runningJar == null) {
			return "Running JAR could not be located";
		}

		try {
			ProcessBuilder process = new ProcessBuilder(
					binPath.getPath() + File.separator + "java", "-jar",
					runningJar.getPath());
			process.start();
			return null;
		} catch (IOException e) {
			return ExceptionUtils.getStackTrace(e);
		}
	}

	/**
	 * Shuts down the application and restarts it if possible.
	 * <p>
	 * If the application cannot be restarted, a dialog will be displayed
	 * indicating that a restart was intended but could not be achieved.
	 */
	public static void restart() {
		String issue = restart0();

		/*
		 * 
		 */
		if (issue != null) {
			String message = "Program failed to restart: \"" + issue
					+ "\"\nPlease reopen the application manually.";
			JOptionPane.showMessageDialog(null, message, "Restart failure",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the program architecture as either 32-bit or 64-bit.
	 * 
	 * @return the program architecture, {@code -1} if it could not be
	 *         determined.
	 */
	public static int getArch() {
		String dataModel = System.getProperty("sun.arch.data.model");
		if (dataModel == null) {
			return -1;
		}

		if (dataModel.contains("32") || dataModel.contains("x86")) {
			return 32;
		} else if (dataModel.contains("64")) {
			return 64;
		}
		return -1;
	}
	
	/**
	 * Attemps to result the operating system this machine is running on.
	 * 
	 * @return the operating system this machine is running on, {@link #UNKNOWN}
	 *         if it could not be determined.
	 */
	public static OperatingSystem getOS() {
		String os_name = System.getProperty("os.name");
		OperatingSystem op_sys = OperatingSystem.UNKNOWN;
		for (OperatingSystem os : OperatingSystem.values()) {
			if (os.isSystem(os_name)) {
				op_sys = os;
				break;
			}
		}
		return op_sys;
	}

	/**
	 * The first 4-<code>byte</code>s found within every ZIP file.
	 */
	private static final int ZIP_FILE_HEADER_MAGIC = 0x504B0304;

	/**
	 * The file that must be present within every JAR file for it to be
	 * considered a valid Java JAR file.
	 */
	private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

	/**
	 * Returns whether or not the specified file is a non-empty ZIP file.
	 * 
	 * @param file
	 *            the file.
	 * @return <code>true</code> if the file is a non-empty ZIP file,
	 *         <code>false</code> otherwise.
	 * @throws RuntimeException
	 *             if an <code>IOException</code> other than a
	 *             <code>EOFException</code> is caught despite the safe checks
	 *             put in place to prevent them.
	 */
	public static boolean isZipFile(File file) throws RuntimeException {
		if (!file.isDirectory()) {
			try {
				DataInputStream input =
						new DataInputStream(new FileInputStream(file));
				int magic = input.readInt();
				input.close();
				return magic == ZIP_FILE_HEADER_MAGIC;
			} catch (EOFException e) {
				return false; // File too small
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	/**
	 * Returns whether or not the specified file is a JAR file.
	 * 
	 * @param file
	 *            the file to check.
	 * @return <code>true</code> if the file is a valid JAR file,
	 *         <code>false</code> otherwise.
	 * @throws RuntimeException
	 *             if an <code>IOException</code> is caught despite the safe
	 *             checks put in place to prevent them.
	 */
	public static boolean isJarFile(File file) throws RuntimeException {
		if (isZipFile(file)) {
			try {
				ZipFile zipFile = new ZipFile(file);
				ZipEntry manifestEntry = zipFile.getEntry(MANIFEST_PATH);
				zipFile.close();
				return manifestEntry != null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	/**
	 * Returns the JAR file that the program is currently running in.
	 * 
	 * @return the JAR file that the program is currently running in,
	 *         <code>null</code> if the file is not a JAR file.
	 */
	public static File getRunningFile() {
		File runningJar = getRunningLocation();
		if (!isJarFile(runningJar)) {
			return null;
		}
		return runningJar;
	}

	/**
	 * Returns the the directory/file the program is currently being run in.
	 * 
	 * @return the the directory/file the program is currently being run in.
	 */
	public static File getRunningLocation() {
		try {
			return new File(Ardenus.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
