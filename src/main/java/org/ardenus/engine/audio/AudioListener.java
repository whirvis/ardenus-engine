package org.ardenus.engine.audio;

import static org.lwjgl.openal.AL10.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Represents the OpenAL listener and can be used to fetch and update their
 * gain, position, velocity, etc. using both 2D and 3D vectors. Both
 * {@link Vector2f} and {@link Vector3f} are supported.
 * 
 * @see Audio
 */
public class AudioListener {

	/* optimization for getListener3f() and others */
	private static final Lock PVAL_LOCK = new ReentrantLock();
	private static final float[][] PVAL = new float[3][1];

	/**
	 * Fetches an OpenAL listener parameter and returns it.
	 * 
	 * @param alParam
	 *            the OpenAL parameter.
	 * @return the value of {@code alParam}.
	 */
	private static float getListenerf(int alParam) {
		Audio.requireInit();
		return alGetListenerf(alParam);
	}

	/**
	 * Sets the value of an OpenAL listener parameter.
	 * 
	 * @param alParam
	 *            the OpenAL parameter.
	 * @param value
	 *            the parameter value.
	 */
	private static void setListenerf(int alParam, float value) {
		Audio.requireInit();
		alListenerf(alParam, value);
	}

	/**
	 * Fetches a 3-dimensional OpenAL listener parameter and stores its
	 * component values into the specified {@code Vector3f}.
	 * <p>
	 * This function exists purely for optimization. Since Java has no
	 * references, LWJGL makes use of arrays to hack in references. However,
	 * this can cumbersome and result in wasted resources. The use of a cached
	 * {@code PVAL} array to store these values before storing them inside a
	 * destination {@code Vector3f} acts as a workaround to this problem.
	 * 
	 * @param alParam
	 *            the OpenAL parameter.
	 * @param dest
	 *            where to store the components.
	 * @return {@code dest} now storing the components of {@code alParam}.
	 */
	private static Vector3f getListener3f(int alParam, Vector3f dest) {
		Audio.requireInit();
		PVAL_LOCK.lock();
		try {
			alGetListener3f(alParam, PVAL[0], PVAL[1], PVAL[2]);
			dest.x = PVAL[0][0];
			dest.y = PVAL[1][0];
			dest.z = PVAL[2][0];
			return dest;
		} finally {
			PVAL_LOCK.unlock();
		}
	}

	/**
	 * Sets the component values of a 3-dimension OpenAL listener parameter.
	 * 
	 * @param alParam
	 *            the OpenAL parameter.
	 * @param x
	 *            the X-axis component of the value.
	 * @param y
	 *            the Y-axis component of the value.
	 * @param z
	 *            the Z-axis component of the value.
	 */
	private static void setListener3f(int alParam, float x, float y, float z) {
		Audio.requireInit();
		alListener3f(alParam, x, y, z);
	}

	/**
	 * Fetches a 3-dimensional OpenAL listener parameter and stores its X-axis
	 * and Y-axis component values into the specified {@code Vector2f}.
	 * <p>
	 * This function exists purely for optimization. Since Java has no
	 * references, LWJGL makes use of arrays to hack in references. However,
	 * this can cumbersome and result in wasted resources. The use of a cached
	 * {@code PVAL} array to store these values before storing them inside a
	 * destination {@code Vector2f} acts as a workaround to this problem.
	 * 
	 * @param alParam
	 *            the OpenAL parameter.
	 * @param dest
	 *            where to store the components.
	 * @return {@code dest} now storing the components of {@code alParam}.
	 */
	private static Vector2f getListener2f(int alParam, Vector2f dest) {
		Audio.requireInit();
		PVAL_LOCK.lock();
		try {
			alGetListener3f(alParam, PVAL[0], PVAL[1], PVAL[2]);
			dest.x = PVAL[0][0];
			dest.y = PVAL[1][0];
			return dest;
		} finally {
			PVAL_LOCK.unlock();
		}
	}

	/**
	 * Returns the listener gain.
	 * 
	 * @return the listener gain.
	 */
	public static float getGain() {
		return getListenerf(AL_GAIN);
	}

	/**
	 * Sets the gain of the listener.
	 * 
	 * @param gain
	 *            the listener gain.
	 */
	public static void setGain(float gain) {
		setListenerf(AL_GAIN, gain);
	}

	/**
	 * Returns the listener position.
	 * 
	 * @param pos
	 *            the {@code Vector3f} to store the position into.
	 * @return {@code pos}, now storing the position.
	 */
	public static Vector3f getPosition(Vector3f pos) {
		return getListener3f(AL_POSITION, pos);
	}

	/**
	 * Returns the listener position.
	 * 
	 * @param pos
	 *            the {@code Vector2f} to store the position into.
	 * @return {@code pos}, now storing the position.
	 */
	public static Vector2f getPosition(Vector2f pos) {
		return getListener2f(AL_POSITION, pos);
	}

	/**
	 * Sets the position of the listener.
	 * 
	 * @param x
	 *            the X-axis position.
	 * @param y
	 *            the Y-axis position.
	 * @param z
	 *            the Z-axis position.
	 */
	public static void setPosition(float x, float y, float z) {
		setListener3f(AL_POSITION, x, y, z);
	}

	/**
	 * Sets the position of the listener.
	 * <p>
	 * This function is a shorthand for
	 * {@link #setPosition(float, float, float)}, with the {@code z} parameter
	 * being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis position.
	 * @param y
	 *            the Y-axis position.
	 */
	public static void setPosition(float x, float y) {
		setPosition(x, y, 0.0F);
	}

	/**
	 * Sets the position of the listener.
	 * <p>
	 * This method is a shorthand for {@link #setPosition(float, float, float)},
	 * with the {@code x}, {@code y}, and {@code z} parameters being the
	 * component vectors of {@code pos}.
	 * 
	 * @param pos
	 *            the position.
	 */
	public static void setPosition(Vector3f pos) {
		setPosition(pos.x, pos.y, pos.z);
	}

	/**
	 * Sets the position of the listener.
	 * <p>
	 * This method is a shorthand for {@link #setPosition(float, float, float)},
	 * with the {@code x} and {@code y} parameters being the component vectors
	 * of {@code pos}, and the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param pos
	 *            the position.
	 */
	public static void setPosition(Vector2f pos) {
		setPosition(pos.x, pos.y, 0.0F);
	}

	/**
	 * Returns the listener velocity.
	 * 
	 * @param vel
	 *            the {@code Vector3f} to store the velocity into.
	 * @return {@code vel}, now storing the velocity.
	 */
	public static Vector3f getVelocity(Vector3f vel) {
		return getListener3f(AL_VELOCITY, vel);
	}

	/**
	 * Returns the listener velocity.
	 * 
	 * @param vel
	 *            the {@code Vector2f} to store the velocity into.
	 * @return {@code vel}, now storing the velocity.
	 */
	public static Vector2f getVelocity(Vector2f vel) {
		return getListener2f(AL_VELOCITY, vel);
	}

	/**
	 * Sets the velocity of the listener.
	 * 
	 * @param x
	 *            the X-axis velocity.
	 * @param y
	 *            the Y-axis velocity.
	 * @param z
	 *            the Z-axis velocity.
	 */
	public static void setVelocity(float x, float y, float z) {
		setListener3f(AL_VELOCITY, x, y, z);
	}

	/**
	 * Sets the velocity of the listener.
	 * <p>
	 * This function is a shorthand for
	 * {@link #setVelocity(float, float, float)}, with the {@code z} parameter
	 * being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis velocity.
	 * @param y
	 *            the Y-axis velocity.
	 */
	public static void setVelocity(float x, float y) {
		setVelocity(x, y, 0.0F);
	}

	/**
	 * Sets the velocity of the listener.
	 * <p>
	 * This method is a shorthand for {@link #setVelocity(float, float, float)},
	 * with the {@code x}, {@code y}, and {@code z} parameters being the
	 * component vectors of {@code vel}.
	 * 
	 * @param vel
	 *            the velocity.
	 */
	public static void setVelocity(Vector3f vel) {
		setVelocity(vel.x, vel.y, vel.z);
	}

	/**
	 * Sets the velocity of the listener.
	 * <p>
	 * This method is a shorthand for {@link #setVelocity(float, float, float)},
	 * with the {@code x} and {@code y} parameters being the component vectors
	 * of {@code vel}, and the {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param vel
	 *            the velocity.
	 */
	public static void setVelocity(Vector2f vel) {
		setVelocity(vel.x, vel.y, 0.0F);
	}

	/**
	 * Returns the listener orientation.
	 * 
	 * @param rot
	 *            the {@code Vector3f} to store the orientation into.
	 * @return {@code rot}, now storing the orientation.
	 */
	public static Vector3f getOrientation(Vector3f rot) {
		return getListener3f(AL_ORIENTATION, rot);
	}

	/**
	 * Returns the listener orientation.
	 * 
	 * @param rot
	 *            the {@code Vector2f} to store the orientation into.
	 * @return {@code rot}, now storing the orientation.
	 */
	public static Vector2f getOrientation(Vector2f rot) {
		return getListener2f(AL_ORIENTATION, rot);
	}

	/**
	 * Sets the orientation of the listener.
	 * 
	 * @param x
	 *            the X-axis orientation.
	 * @param y
	 *            the Y-axis orientation.
	 * @param z
	 *            the Z-axis orientation.
	 */
	public static void setOrientation(float x, float y, float z) {
		setListener3f(AL_ORIENTATION, x, y, z);
	}

	/**
	 * Sets the orientation of the listener.
	 * <p>
	 * This function is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code z}
	 * parameter being set to {@code 0.0F}.
	 * 
	 * @param x
	 *            the X-axis orientation.
	 * @param y
	 *            the Y-axis orientation.
	 */
	public static void setOrientation(float x, float y) {
		setOrientation(x, y, 0.0F);
	}

	/**
	 * Sets the orientation of the listener.
	 * <p>
	 * This method is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code x},
	 * {@code y}, and {@code z} parameters being the component vectors of
	 * {@code rot}.
	 * 
	 * @param rot
	 *            the orientation.
	 */
	public static void setOrientation(Vector3f rot) {
		setOrientation(rot.x, rot.y, rot.z);
	}

	/**
	 * Sets the orientation of the listener.
	 * <p>
	 * This method is a shorthand for
	 * {@link #setOrientation(float, float, float)}, with the {@code x} and
	 * {@code y} parameters being the component vectors of {@code rot}, and the
	 * {@code z} parameter being set to {@code 0.0F}.
	 * 
	 * @param rot
	 *            the orientation.
	 */
	public static void setOrientation(Vector2f rot) {
		setOrientation(rot.x, rot.y, 0.0F);
	}

}
