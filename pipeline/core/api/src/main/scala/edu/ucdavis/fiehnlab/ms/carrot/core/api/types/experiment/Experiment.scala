package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass

/**
  * Created by wohlgemuth on 6/23/16.
  */
final case class Experiment(

                             /**
                               * relates classes for this experiment
                               */
                             classes: Seq[_ <: ExperimentClass],


                             /**
                               * name of this experiment
                               */
                             name: Option[String] = None,

                             /**
                               * associated acquisition method
                               */
                             acquisitionMethod: AcquisitionMethod
                           )