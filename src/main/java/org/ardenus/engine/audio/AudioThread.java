package org.ardenus.engine.audio;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.ardenus.engine.audio.sound.Sound;

public class AudioThread extends Thread {

	protected Set<Sound> sounds;
	protected Queue<Sound> abandoned;

	protected AudioThread() {
		/*
		 * A synchronized set looks overkill here at first glance. However,
		 * every now and then a NullPointerException will get thrown when this
		 * thread is started unless this set is synchronized. The exact cause
		 * for this is currently unknown.
		 */
		this.sounds = Collections.synchronizedSet(new HashSet<Sound>());
	}

	public void maintain(Sound sound) {
		Objects.requireNonNull(sound, "sound");
		sounds.add(sound);
	}

	public void abandon(Sound sound) {
		abandoned.add(sound);
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				/* hack to lower CPU usage */
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
				this.interrupt();
			}
			
			Iterator<Sound> abandonedI = abandoned.iterator();
			while(abandonedI.hasNext()) {
				sounds.remove(abandonedI.next());
				abandonedI.remove();
			}

			Iterator<Sound> soundI = sounds.iterator();
			while (soundI.hasNext()) {
				try {
					soundI.next().update();
				} catch (Exception e) {
					Audio.LOG.error("Error updating sound", e);
					soundI.remove();
				}
			}
		}
	}

}
