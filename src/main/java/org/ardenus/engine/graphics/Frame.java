package org.ardenus.engine.graphics;

public class Frame extends Image {

	private long duration;

	public Frame(boolean preserveTexture, int h_glTexture, long duration) {
		super(preserveTexture, h_glTexture);
		this.setDuration(duration);
	}

	public Frame(int h_glTexture, long duration) {
		super(h_glTexture);
		this.setDuration(duration);
	}

	public Frame(long duration) {
		this.setDuration(duration);
	}
	
	public Frame() {
		this(-1);
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
