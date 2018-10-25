package edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * used to define that this is a possible binbase variable for system runtime
 * @author wohlgemuth
 *
 */
@Target(value={ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemVariable {

	/**
	 * descriptopn of this variable
	 * @return
	 */
	String description();
	
	/**
	 * a short description of possible values
	 * @return
	 */
	String possibleValues();
}
