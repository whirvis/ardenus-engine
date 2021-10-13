package org.ardenus.engine.input;

/**
 * Signals that a Unicode character has been typed.
 */
public class UnicodeInputEvent extends InputEvent {

	private final Object source;
	private final char c;

	/**
	 * Constructs a new {@code UnicodeInputEvent}.
	 * 
	 * @param source
	 *            the source of input, may be {@code null}.
	 * @param c
	 *            the character type.
	 */
	public UnicodeInputEvent(Object source, char c) {
		this.source = source;
		this.c = c;
	}
	
	/**
	 * Constructs a new {@code UnicodeInputEvent}.
	 * 
	 * @param c
	 *            the character type.
	 */
	public UnicodeInputEvent(char c) {
		this(null, c);
	}
	
	/**
	 * Returns the source of input.
	 * 
	 * @return the source of input, if any.
	 */
	public Object getSource() {
		return this.source;
	}

	/**
	 * Returns the typed character.
	 * 
	 * @return the typed character.
	 */
	public char getChar() {
		return this.c;
	}

}
