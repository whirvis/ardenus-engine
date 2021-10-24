package org.ardenus.engine.audio;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardenus.engine.audio.sound.Sound;

public class AudioThread extends Thread {

	private final Set<Sound> sounds;
	private final Queue<Sound> abandoned;
	private final Lock updateLock;

	protected AudioThread() {
		/*
		 * A synchronized set looks overkill here at first glance. However,
		 * every now and then a NullPointerException will get thrown when this
		 * thread is started unless this set is synchronized. The exact cause
		 * for this is currently unknown.
		 */
		this.sounds = Collections.synchronizedSet(new HashSet<Sound>());
		this.abandoned = new LinkedList<>();
		this.updateLock = new ReentrantLock();
	}

	public void maintain(Sound sound) {
		Objects.requireNonNull(sound, "sound");
		sounds.add(sound);
	}

	public void abandon(Sound sound) {
		abandoned.add(sound);
	}

	private void update() {
		Iterator<Sound> abandonedI = abandoned.iterator();
		while (abandonedI.hasNext()) {
			sounds.remove(abandonedI.next());
			abandonedI.remove();
		}

		Iterator<Sound> soundI = sounds.iterator();
		while (soundI.hasNext()) {
			try {
				Sound sound = soundI.next();
				sound.update();
			} catch (Exception e) {
				Audio.LOG.error("Error updating sound", e);
				soundI.remove();
			}
		}
	}

	@Override
	public void interrupt() {
		updateLock.lock();
		try {
			super.interrupt();
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				/* hack to lower CPU usage */
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
				this.interrupt();
				continue;
			}

			/*
			 * During shutdown, it is common for this thread to be in the midst
			 * of updating other sounds before it reaches its interrupt signal.
			 * This causes for crucial audio systems, like OpenAL, to shutdown
			 * before the final sound updates can be processed. The consequences
			 * of this can range from a warning message to possible program
			 * crashes. As such, an update lock is required here.
			 */
			try {
				updateLock.lock();
				this.update();
			} finally {
				updateLock.unlock();
			}
		}
	}

}
