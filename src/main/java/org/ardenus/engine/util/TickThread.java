package org.ardenus.engine.util;

import java.time.Duration;
import java.util.Objects;

/**
 * A thread which runs tick cycles forever until interrupted.
 * 
 * @see #onTick(long)
 * @see #onInterrupt()
 */
public abstract class TickThread extends Thread {

	private long tickMillis;
	private long lastTick;

	/**
	 * Constructs a new {@code TickThread}.
	 * 
	 * @param tick
	 *            how long to wait in between in each call to
	 *            {@link #onTick(long)}.
	 * @throws NullPointerException
	 *             if {@code tick} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code tick} is negative.
	 */
	public TickThread(Duration tick) {
		Objects.requireNonNull(tick, "tick");
		if (tick.isNegative()) {
			throw new IllegalArgumentException("negative tick");
		}
		this.tickMillis = tick.toMillis();
	}

	/**
	 * Constructs a new {@code TickThread} with no wait between ticks.
	 */
	public TickThread() {
		this(Duration.ZERO);
	}

	/**
	 * Called each cycle of the run loop.
	 * 
	 * @param delta
	 *            the delta in milliseconds.
	 */
	protected abstract void onTick(long delta);

	/**
	 * Called when this thread is interrupted.
	 */
	protected void onInterrupt() {
		/* optional implement */
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			long currentTime = System.currentTimeMillis();
			long delta = lastTick <= 0 ? 0 : lastTick - currentTime;
			this.lastTick = currentTime;

			this.onTick(delta);
			try {
				Program.lowerUsage(this);
				Thread.sleep(tickMillis);
			} catch (InterruptedException e) {
				this.interrupt();
			}
		}
		this.onInterrupt();
	}

}
