package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.math.similarity.{CompositeSimilarity, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition.AcquisitionLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, MSMSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRangePPM, IncludeByRetentionIndexTimeWindow}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 7/12/17.
  */
@Component
@Profile(Array("dynamic-library"))
class AddToLibraryAction @Autowired()(val targets: LibraryAccess[Target]) extends PostAction with LazyLogging {

  @Autowired
  val acquisitionLoader: AcquisitionLoader = null

  /**
    * which similarity to use in the system
    */
  @Autowired(required = false)
  val similarity: Similarity = new CompositeSimilarity

  /**
    * minimum required similarity
    */
  @Value("${carrot.msms.generate.library.similarity.min:700}")
  val minimumSimilarity: Double = 700

  @Value("${carrot.msms.generate.library.retentionIndex.window:1}")
  val retentionIndexWindow: Double = 1

  @Value("${carrot.msms.generate.library.accurateMass.window:5}")
  val accurateMassWindow: Double = 700

  /**
    * executes this action
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  override def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    val method = acquisitionLoader.load(sample).get
    sample match {
      case data: AnnotatedSample =>

        data.noneAnnotated.foreach { x =>
          addTargetToLibrary(x, data, method)
        }

      case _ =>
        logger.warn(s"action not applicable for this sample: $sample")
    }
  }

  /**
    * add this feature to the library, if certain criteria are met.
    *
    * @param t
    * @param sample
    * @param acquisitionMethod
    */
  def addTargetToLibrary(t: Feature with CorrectedSpectra, sample: AnnotatedSample, acquisitionMethod: AcquisitionMethod) = {

    t match {
      case target: MSMSSpectra =>

        if (target.massOfDetectedFeature.isDefined) {
          logger.info(s"creating new target from feature: ${t}")

          val newTarget = new Target {
            /**
              * by default we report the retention time the same as the retention index
              * unless overwritten
              */
            override val retentionTimeInSeconds: Double = target.retentionTimeInSeconds

            /**
              * the unique inchi key for this spectra
              */
            override val inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = target.retentionIndex
            /**
              * a name for this spectra
              */
            override val name: Option[String] = None
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(target.precursorIon)
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
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = target.spectrum

          }

          if (!targetAlreadyExists(newTarget, acquisitionMethod)) {
            targets.add(
              newTarget

              , acquisitionMethod
            )
          }
          else {
            logger.warn(s"the target you attempted to generate already exists! ${newTarget}")
          }
        }
        else {
          logger.info(s"target has no mass associated, so it's not valid: ${
            target
          }")
        }

      case _ =>
        logger.debug(s"${t} is not an MSMS spectra and so can't be considered to become a new target!")
    }
  }

  /**
    * does this target already exist in the remote system
    *
    * @param newTarget
    * @return
    */
  def targetAlreadyExists(newTarget: Target, acquisitionMethod: AcquisitionMethod): Boolean = {
    //we only accept MS2 and higher for this
    val toMatch = targets.load(acquisitionMethod).filter(_.spectrum.isDefined).filter(_.spectrum.get.msLevel > 1)

    //RI filter
    val riFilter = new IncludeByRetentionIndexTimeWindow(newTarget.retentionIndex,retentionIndexWindow)

    val filteredByRi = toMatch.filter(riFilter.include)

    logger.info(s"after ri filter: ${filteredByRi.size} are left")
    //MASS filter
    val massFilter = new IncludeByMassRangePPM(newTarget,accurateMassWindow)

    //Similarity filter
    val filtedByMass = filteredByRi.filter(massFilter.include)

    logger.info(s"after mass filter: ${filtedByMass.size} are left")

    false
  }
}
