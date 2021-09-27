package org.ardenus.engine.input.device.adapter;

import java.util.Objects;

import org.ardenus.engine.input.device.analog.DeviceAnalog;

/**
 * A {@link DeviceAnalog} mapping for use with a {@link DeviceAdapter}.
 * <p>
 * On their own, a mapped analog can not provide a meaningful mapping for a
 * device analog. It must be extended by a class which provides information
 * meaningful to the context of a relevant device adapter. This can be as simple
 * as providing an extra field for an analog ID. An example of this would be:
 * 
 * <pre>
 * public class GLFWMappedAnalog extends MappedAnalog {
 * 
 * 	public final int glfwAxis;
 * 
 * 	public GLFWMappedAnalog(DeviceAnalog analog, int glfwAxis) {
 * 		super(analog);
 * 		this.glfwAxis = glfwAxis;
 * 	}
 * 
 * }
 * </pre>
 * 
 * @param <A>
 *            the analog input type.
 * @see AnalogMapping
 */
public abstract class MappedAnalog<A extends DeviceAnalog<?>> {

	public final A analog;

	/**
	 * Constructs a new {@code MappedAnalog}.
	 * 
	 * @param analog
	 *            the analog being mapped to.
	 * @throws NullPointerException
	 *             if {@code analog} is {@code null}.
	 */
	public MappedAnalog(A analog) {
		this.analog = Objects.requireNonNull(analog, "analog");
	}

}
