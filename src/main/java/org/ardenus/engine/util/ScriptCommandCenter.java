package org.ardenus.engine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.whirvex.args.ArgsException;
import com.whirvex.cmd.CommandCenter;
import com.whirvex.cmd.CommandSender;
import com.whirvex.cmd.input.InputException;
import com.whirvex.event.EventManager;

/**
 * A script-enabled command center which can execute text files containing
 * commands. To prevent ambiguous scripts, command aliases are not recognized by
 * this command center.
 */
public class ScriptCommandCenter extends CommandCenter {

	private boolean executing;

	/**
	 * Creates a standard script-enabled command center.
	 * 
	 * @param name
	 *            the name of this command center, to be used in logging.
	 * @param events
	 *            the event manager to send events to, may be {@code null}.
	 * @throws NullPointerException
	 *             if {@code name} is {@code null}.
	 */
	public ScriptCommandCenter(String name, EventManager events) {
		super(name, events);
		super.setRecognizeAliases(false);
	}

	/**
	 * Creates a standard script-enabled command center.
	 * 
	 * @param name
	 *            the name of this command center, to be used in logging.
	 * @throws NullPointerException
	 *             if {@code name} is {@code null}.
	 */
	public ScriptCommandCenter(String name) {
		this(ScriptCommandCenter.class.getSimpleName(), null);
	}

	/**
	 * Creates a standard script-enabled command center.
	 * 
	 * @param events
	 *            the event manager to send events to, may be {@code null}.
	 */
	public ScriptCommandCenter(EventManager events) {
		this(ScriptCommandCenter.class.getSimpleName(), events);
	}

	/**
	 * Creates a standard script-enabled command center.
	 */
	public ScriptCommandCenter() {
		this(ScriptCommandCenter.class.getSimpleName(), null);
	}

	@Override
	public void setRecognizeAliases(boolean recognizeAliases) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void onInvalidInput(CommandSender sender, InputException ie) {
		super.onInvalidInput(sender, ie);
		if (executing == true) {
			throw ie; /* program error */
		}
	}

	@Override
	protected void onInvalidArgs(CommandSender sender, ArgsException ae) {
		super.onInvalidArgs(sender, ae);
		if (executing == true) {
			throw ae; /* program error */
		}
	}

	/**
	 * Executes a script file containing commands.
	 * <p>
	 * Lines that begin with {@code "#"} will be treated as comments. Lines that
	 * end with a backslash (and are not a comment) will have the next line
	 * considered part of the same command.
	 * 
	 * @param script
	 *            the script file to execute.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws InputException
	 *             if an input error occurs.
	 * @throws ArgsException
	 *             if an arguments error occurs.
	 */
	public synchronized void executeScript(File script) throws IOException {
		this.executing = true;
		BufferedReader reader = new BufferedReader(new FileReader(script));
		StringBuilder cmd = new StringBuilder();

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty()) {
				continue; /* nothing to process */
			} else if (line.startsWith("#")) {
				continue; /* line is a comment */
			}

			/*
			 * If this line ends with a backslash, then the next line should be
			 * considered as part of the same command. This allows for more
			 * complex yet readable commands in scripts.
			 */
			boolean contLine = false;
			if (line.endsWith("\\")) {
				line = line.substring(0, line.length() - 1);
				contLine = true;
			}

			cmd.append(line.trim() + (contLine ? " " : ""));
			if (contLine == false) {
				this.execute(cmd.toString());
				cmd.setLength(0);
			}
		}

		reader.close();
		this.executing = false;
	}

}
