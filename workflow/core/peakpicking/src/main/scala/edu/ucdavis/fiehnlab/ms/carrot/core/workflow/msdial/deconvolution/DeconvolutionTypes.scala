package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.Peak

/**
  * Created by diego on 7/21/2016.
  */
case class IonCandidate( ion: Ion,
                         firstDiff: Double,
                         secondDiff: Double
                       )

case class BasePeak( override val scanNumber: Int,
                     override val retentionTimeInMinutes: Double,
                     override val maxMass: Double,
                     override val sumIntensity: Double
                   ) extends Peak
