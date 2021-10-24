package org.ardenus.engine.graphics.shader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When present, signals to {@link Program} that a field inside a class or an
 * object instance should contain the location of a uniform. At the end of
 * linking, the program will resolve these fields automatically for extending
 * classes. Resolution in other locations must be done manually.
 * 
 * @see Program#resolveUniformLocs(Class)
 * @see Program#resolveUniformLocs(Object)
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Uniform {

	/**
	 * By default, this will return an empty string.<br>
	 * In this scenario, field name is assumed to be the uniform name.
	 * 
	 * @return the uniform name, may be an empty string.
	 */
	public String value() default "";

	/**
	 * By default, this will return {@code false}.<br>
	 * In this scenario, the uniform is expected to be present.
	 * 
	 * @return {@code true} if the uniform may be absent, {@code false}
	 *         otherwise.
	 */
	boolean optional() default false;

}
