package edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * defines that this action needs to be run before the netcdf replacement
 * @author wohlgemuth
 *
 */
public  @Retention(RetentionPolicy.RUNTIME) @interface PreActionOnly {

}
