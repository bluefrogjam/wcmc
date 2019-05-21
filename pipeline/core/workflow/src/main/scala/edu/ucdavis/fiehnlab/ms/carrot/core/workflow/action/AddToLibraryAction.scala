package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.math.similarity.{CompositeSimilarity, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRangePPM, IncludeByRetentionIndexWindow, IncludeBySimilarity}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 7/12/17.
  */
@Component
@Profile(Array("carrot.targets.dynamic"))
class AddToLibraryAction @Autowired()(val targets: LibraryAccess[Target]) extends PostAction with Logging {

  /**
    * which similarity to use in the system
    */
  @Autowired(required = false)
  val similarity: Similarity = new CompositeSimilarity

  /**
    * minimum required similarity
    */
  @Value("${wcmc.workflow.lcms.msms.generate.library.similarity.min:0.7}")
  val minimumSimilarity: Double = 0

  /**
    * library inclusion time window in seconds
    */
  @Value("${wcmc.workflow.lcms.msms.generate.library.retentionIndex.window:6}")
  val retentionIndexWindow: Double = 0

  /**
    * mass window in PPM
    */
  @Value("${wcmc.workflow.lcms.msms.generate.library.accurateMass.window:10}")
  val accurateMassWindow: Double = 0

  @Value("${wcmc.workflow.lcms.msms.generate.library.intensity.min: 1000}")
  val minimumRequiredIntensity: Double = 0

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
        logger.info(s"adding ${data.noneAnnotated.count(_.isInstanceOf[MSMSSpectra])} unannotated msms from ${sample.name} to mona")
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

          val newTarget = new Target with PrecursorSupport {

            override val uniqueMass: Option[Double] = t.uniqueMass
            override val retentionTimeInSeconds: Double = target.retentionTimeInSeconds
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = target.retentionIndex
            override var name: Option[String] = None
            override val precursorMass: Option[Double] = Option(target.precursorIon)
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = target.associatedScan
            override val precursorScan: Option[SpectrumProperties] = target.precursorScan

          }

          if (!targetAlreadyExists(newTarget, acquisitionMethod, sample)) {
            targets.add(target2mona(newTarget), acquisitionMethod, Some(sample))
          }
          else {
            logger.warn(s"the target you attempted to generate already exists! ${newTarget}")
          }
        }
        else {
          logger.warn(s"target has no mass associated, so it's not valid: ${target}")
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
    val riFilter = new IncludeByRetentionIndexWindow(newTarget.retentionIndex, retentionIndexWindow)

    val massFilter = new IncludeByMassRangePPM(newTarget, accurateMassWindow)

    val similarityFilter = new IncludeBySimilarity(newTarget, minimumSimilarity)

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


  private def target2mona(target: Target with PrecursorSupport): MonaLibraryTarget = {
    MonaLibraryTarget(target.spectrum.get.splash,
      target.spectrum.get,
      target.name,
      target.retentionIndex,
      target.retentionTimeInSeconds,
      target.inchiKey,
      target.precursorMass,
      confirmed = false,
      requiredForCorrection = false,
      isRetentionIndexStandard = false,
      target.ionMode,
      target.uniqueMass
    )
  }
}
