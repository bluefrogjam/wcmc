package edu.ucdavis.genomics.metabolomics.binbase.annotations

import java.lang.reflect.Method

/**
 * Date: Jul 1, 2010
 * Time: 4:56:08 PM
 * To change this template use File | Settings | File Templates.
 */
class AnnotationHelper {

  /**
   * generates the list of annotations in this class
   */
  static Collection<QueryObject> getQueryableAnnotations(Class c) {

    //contains the result
    Set<QueryObject> set = new HashSet<QueryObject>()

    for (Method method: c.getMethods()) {
      if (method.isAnnotationPresent(Queryable.class)) {
        Queryable q = method.getAnnotation(Queryable.class)

        QueryObject object = new QueryObject()
        object.setAnnotation(q)
        object.setMethod(method)

        set.add(object)
      }
    }

    return set
  }
  
}
