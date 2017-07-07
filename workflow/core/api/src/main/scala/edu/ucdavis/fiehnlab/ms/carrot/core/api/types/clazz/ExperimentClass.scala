package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz

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
                                    * optional name for this class
                                    */
                                  name: Option[String] = None,

                                  /**
                                    * name of the organ
                                    */
                                  organ: Option[String] = None,

                                  /**
                                    * species name
                                    */
                                  species: Option[String] = None,

                                  /**
                                    * associated treamtnets
                                    */
                                  treatments: Option[Seq[String]] = None
                                )

