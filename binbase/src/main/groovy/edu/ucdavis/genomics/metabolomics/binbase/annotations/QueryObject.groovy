package edu.ucdavis.genomics.metabolomics.binbase.annotations


import java.lang.reflect.Method

/**
 * Date: Jul 1, 2010
 * Time: 4:50:45 PM
 * To change this template use File | Settings | File Templates.
 */
class QueryObject implements Comparable{

  Queryable annotation

  Method method

  int compareTo(Object o) {
    return method.getName().compareTo(o.get);
  }

  /**
   * @return the annotation
   */
  public Queryable getAnnotation() {
      return annotation;
  }
  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(Queryable annotation) {
      this.annotation = annotation;
  }
  /**
   * @return the method
   */
  public Method getMethod() {
      return method;
  }
  /**
   * @param method the method to set
   */
  public void setMethod(Method method) {
      this.method = method;
  }
}
