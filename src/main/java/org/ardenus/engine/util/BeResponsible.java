package org.ardenus.engine.util;

import java.lang.annotation.Documented;

/**
 * An annotation which warns the programmer that the code being called
 * <b>performs no checks</b> to ensure validity of context, state, parameters,
 * etc. The reasons for this will probably vary. However, the usual reason is
 * for a speed advantage (e.g., graphics rendering, audio processing, etc.)
 * <p>
 * A value for this annotation can be specified to explain exactly the
 * programmer must "be responsible" when calling the annotated code. By default,
 * {@link #value()} defaults to {@code "performance"}.
 */
@Documented
public @interface BeResponsible {

	/* Razon? Gato sin lag. */
	String value() default "performance";

}
