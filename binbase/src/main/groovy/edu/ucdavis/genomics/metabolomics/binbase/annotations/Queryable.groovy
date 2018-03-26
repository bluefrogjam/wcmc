package edu.ucdavis.genomics.metabolomics.binbase.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import java.lang.annotation.Inherited

/**
 * Date: Jul 1, 2010
 * Time: 4:52:35 PM
 * used to determine if this method is queryable by the transform server
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Queryable {

  /**
   * the name of the field
   */
  String name()
}