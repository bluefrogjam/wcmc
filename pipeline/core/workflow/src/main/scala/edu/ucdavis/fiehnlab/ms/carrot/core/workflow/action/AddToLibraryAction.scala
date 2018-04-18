package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.math.similarity.{CompositeSimilarity, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONSampleLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, MSMSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRangePPM, IncludeByRetentionIndexTimeWindow, IncludeBySimilarity}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 7/12/17.
  */
@Component
@Profile(Array("carrot.targets.dynamic"))
class AddToLibraryAction @Autowired()(val targets: LibraryAccess[Target]) extends PostAction with LazyLogging {

  /**
    * which similarity to use in the system
    */
  @Autowired(required = false)
  val similarity: Similarity = new CompositeSimilarity

  /**
    * minimum required similarity
    */
  @Value("${carrot.msms.generate.library.similarity.min:0.7}")
  val minimumSimilarity: Double = 0.7

  @Value("${carrot.msms.generate.library.retentionIndex.window:6}")
  val retentionIndexWindow: Double = 1

  @Value("${carrot.msms.generate.library.accurateMass.window:5}")
  val accurateMassWindow: Double = 700

  @Value("${carrot.msms.generate.library.intensity.min:0}")
  val minimumRequiredIntensity: Double = 700

  /**
    * executes this action
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  override def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    val method = experiment.acquisitionMethod
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

            override val uniqueMass: Option[Double] = t.uniqueMass

            /**
              * by default we report the retention time the same as the retention index
              * unless overwritten
              */
            override val retentionTimeInSeconds: Double = target.retentionTimeInSeconds

            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = target.retentionIndex
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(target.precursorIon)
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = target.spectrum

          }

          if (!targetAlreadyExists(newTarget, acquisitionMethod, sample)) {
            targets.add(
              newTarget

              , acquisitionMethod, None
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
  def targetAlreadyExists(newTarget: Target, acquisitionMethod: AcquisitionMethod, sample: Sample): Boolean = {
    val riFilter = new IncludeByRetentionIndexTimeWindow(newTarget.retentionIndex, "targetGeneration", retentionIndexWindow) with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = sample.fileName
    }
    val massFilter = new IncludeByMassRangePPM(newTarget, accurateMassWindow, "targetGeneration") with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = sample.fileName
    }

    val similarityFilter = new IncludeBySimilarity(newTarget, minimumSimilarity, "targetGeneration") with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = sample.fileName
    }


    //we only accept MS2 and higher for this
    val toMatch = targets.load(acquisitionMethod).filter(_.spectrum.isDefined)


    //MS1+ spectra filter
    val msmsSpectra = toMatch.filter(_.spectrum.get.msLevel > 1)
    val filteredByRi = msmsSpectra.filter(riFilter.include(_, applicationContext))
    val filtedByMass = filteredByRi.filter(massFilter.include(_, applicationContext))
    val filteredBySimilarity = filtedByMass.filter(similarityFilter.include(_, applicationContext))

    logger.debug(s"existing targets: ${toMatch.size}")
    logger.debug(s"after MS level filter: ${msmsSpectra.size} targets are left")
    logger.debug(s"after ri filter: ${filteredByRi.size} targets are left")
    logger.debug(s"after mass filter: ${filtedByMass.size} targets are left")
    logger.debug(s"after similarity filter: ${filteredBySimilarity.size} targets are left")

    filteredBySimilarity.nonEmpty
  }
}
