package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 7/12/17.
  */
@Component
@Profile(Array("dynamicLibrary"))
class AddToLibraryAction @Autowired()(val targets: LibraryAccess[Target]) extends PostAction with LazyLogging {
  /**
    * executes this action
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  override def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    sample match {
      case data: AnnotatedSample =>

        data.noneAnnotated.foreach { x =>
          addTargetToLibrary(x, data)
        }

      case _ =>
        logger.warn(s"action not applicable for this sample: $sample")
    }
  }

  def addTargetToLibrary(target: Feature with CorrectedSpectra, sample: AnnotatedSample) = {

    if(target.massOfDetectedFeature.isDefined) {
      targets.add(new Target {
        /**
          * the unique inchi key for this spectra
          */
        override val inchiKey: Option[String] = None
        /**
          * retention time in seconds of this target
          */
        override val retentionTimeInSeconds: Double = target.retentionIndex
        /**
          * a name for this spectra
          */
        override val name: Option[String] = None
        /**
          * the mono isotopic mass of this spectra
          */
        override val precursorMass: Option[Double] = Some(target.massOfDetectedFeature.get.mass)
        /**
          * is this a confirmed target
          */
        override val confirmed: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override val requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override val isRetentionIndexStandard: Boolean = false
      },new AcquisitionMethod(None,None))
    }
    else{
      logger.info(s"target has no mass associated, so it's not valid: ${target}")
    }
  }
}
