package org.ardenus.engine.audio;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.ardenus.engine.audio.sound.Sound;
import org.ardenus.engine.util.TickThread;

public class AudioThread extends TickThread {

	protected Set<Sound> sounds;

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
		sounds.remove(sound);
	}

	@Override
	protected void onTick(long delta) {
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
