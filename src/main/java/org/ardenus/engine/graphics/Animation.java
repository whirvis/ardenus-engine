package org.ardenus.engine.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An animation based on an {@link Image} sequence.
 */
public class Animation {

	private final List<Frame> frames;
	
	private int currentIndex = 0;
	private long frameUpdate = 0;
	private boolean looping;
	private boolean finished;

	public Animation() {
		this.frames = new ArrayList<Frame>();
	}
	
	public List<Frame> getFrames() {
		return Collections.unmodifiableList(frames);
	}
	
	public Frame getFrame(int index) {
		if(index < 0) {
			index += frames.size();
		}
		return frames.get(index);
	}
	
	public Animation setLooping(boolean looping) {
		this.looping = looping;
		return this;
	}

	public Animation addFrame(int index, Frame frame) {
		Objects.requireNonNull(frame, "frame");
		frames.add(index, frame);
		return this;
	}

	public Animation addFrame(Frame frame) {
		return this.addFrame(frames.size(), frame);
	}

	public Animation addFrames(int index, Frame... frames) {
		Objects.requireNonNull(frames, "frames");
		if (frames.length <= 0) {
			throw new IllegalArgumentException("no frames");
		}
		for (Frame frame : frames) {
			this.addFrame(index++, frame);
		}
		return this;
	}

	public Animation addFrames(Frame... frames) {
		return this.addFrames(this.frames.size(), frames);
	}

	public Animation removeFrame(int index) {
		frames.remove(index);
		return this;
	}

	public Animation removeFrame(Frame frame) {
		frames.remove(frame);
		return this;
	}

	public Animation removeFrames(Frame... frames) {
		if (frames != null) {
			if (frames.length <= 0) {
				this.frames.clear();
			}
			for (Frame frame : frames) {
				this.removeFrame(frame);
			}
		}
		return this;
	}
	
	public Image setCurrentFrameIndex(int index) {
		
		Frame previous = frames.get(currentIndex);
		this.currentIndex = index;
		if (currentIndex >= frames.size()) {
			this.currentIndex = looping ? 0 : frames.size();
		} else if (index <= 0) {
			this.currentIndex = 0;
		}
		
		this.frameUpdate -= previous.getDuration();
		return previous;
	}
	
	public void reset() {
		this.setCurrentFrameIndex(0);
		finished = false;
	}
	
	public boolean isFinished() {
		return this.finished;
	}
	
	public Image nextFrame() {
		if(currentIndex + 1 >= frames.size() && !looping) {
			this.finished = true;
			return this.getCurrentFrame();
		}
		return this.setCurrentFrameIndex(currentIndex + 1);
	}

	public void update(long delta) {
		if(finished) {
			return;
		}
		Frame currentFrame = frames.get(currentIndex);
		this.frameUpdate += delta;
		if (frameUpdate >= currentFrame.getDuration()) {
			this.nextFrame();
		}
	}
	
	public Frame getCurrentFrame() {
		return frames.get(currentIndex);
	}

}
