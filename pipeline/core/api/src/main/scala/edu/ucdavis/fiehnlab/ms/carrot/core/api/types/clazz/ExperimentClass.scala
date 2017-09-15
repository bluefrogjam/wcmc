package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.Matrix
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by wohlgemuth on 6/23/16.
  */
final case class ExperimentClass(

                                  /**
                                    * relates samples to this class
                                    */
                                  samples: Seq[_ <: Sample],

                                  /**
                                    * matrix metadata for this class
                                    */
                                  matrix: Option[Matrix]

                                )

