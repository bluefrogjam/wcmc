package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.math.similarity.{CompositeSimilarity, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRange, IncludeByRetentionIndexWindow, IncludeBySimilarity}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 7/12/17.
  */
@Component
@Profile(Array("carrot.targets.dynamic"))
class AddToLibraryAction @Autowired()(val targets: MergeLibraryAccess, val targetFilters: Array[Filter[Target]] = null) extends PostAction with Logging {
  logger.info(s"Creating Action instance with libraries: ${targets.libraries.mkString(", ")}")

  @Value("${carrot.filters.minIonCount:3}")
  val minIonCount: Int = 0

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
  @Value("${wcmc.workflow.lcms.msms.generate.library.accurateMass.window:0.010}")
  val accurateMassWindow: Double = 0

  @Value("${wcmc.workflow.lcms.msms.generate.library.intensity.min: 1000}")
  val minimumRequiredIntensity: Double = 0

  @Autowired
  val stasis: StasisService = null

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  def run(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    logger.info(s"running AddToLibrary Action")

    val metaData: Map[String, AnyRef] = stasis.getAcquisition(sample.getFileName()) match {
      case Some(x) =>
        Map(
          "instrument" -> x.acquisition.instrument,
          "ionisation" -> x.acquisition.ionisation,
          "method" -> x.acquisition.method,
          "organ" -> x.metadata.organ,
          "species" -> x.metadata.species,
          "label" -> x.userdata.label,
          "comment" -> x.userdata.comment


        )
      case None => Map.empty
    }


    sample match {
      case data: QuantifiedSample[Double] =>

        logger.debug(s"adding ${data.noneAnnotated.count(_.isInstanceOf[MSMSSpectra])} unannotated msms from ${sample.name}")
        val libraryTgts: Iterable[Target] = targets.load(experiment.acquisitionMethod, Some(false)).filter(_.spectrum.isDefined) ++ targets.load(experiment.acquisitionMethod).filter(_.spectrum.isDefined)

        logger.debug(s"library targets => confirmed: ${libraryTgts.count(_.confirmed)}\tunconfirmed: ${libraryTgts.count(!_.confirmed)}")

        val newTargets: Seq[AnnotationTarget] = data.noneAnnotated.collect {
          case spec: CorrectedSpectra with MSMSSpectra =>
            new AnnotationTarget() with PrecursorSupport with MetaDataSupport {
              override var name: Option[String] = None
              override val retentionIndex: Double = spec.retentionIndex
              override var inchiKey: Option[String] = None
              override val precursorMass: Option[Double] = Some(spec.precursorIon.get.mass)
              override val uniqueMass: Option[Double] = spec.uniqueMass
              override var confirmed: Boolean = false
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = false
              override val spectrum: Option[SpectrumProperties] = spec.associatedScan
              override val precursorScan: Option[SpectrumProperties] = spec.precursorScan
              override val retentionTimeInMinutes: Double = spec.retentionTimeInMinutes
              override val accurateMass: Option[Double] = spec.accurateMass
              override val metadata: Map[String, AnyRef] = spec.metadata ++ metaData
            }
        }

        logger.debug(s"${newTargets.size} unfiltered unknowns")

        val filteredTgts = newTargets.filter(t => t.spectrum.isDefined && t.spectrum.nonEmpty)
          // each new MSMS target candidate has to pass ALL the filters defined in targetFilters (autowired)
          // currently IonCount and IonHeight filters are applied
          .filter(tgt => targetFilters.forall(_.include(tgt, applicationContext)))

          .filterNot { tgt => targetAlreadyExists(tgt, experiment.acquisitionMethod, libraryTgts) }

        logger.info(s"${filteredTgts.size} filtered unknowns to add")

        targets.add(filteredTgts, experiment.acquisitionMethod, Some(sample))

      case _ => logger.info(s"no MSMS spectra in sample ${sample.name}")
    }
  }

  /**
    * does this target already exist in the remote system
    *
    * @param newTarget
    * @return
    */
  def targetAlreadyExists(newTarget: Target, acquisitionMethod: AcquisitionMethod, unconfirmed: Iterable[Target]): Boolean = {

    val riFilter = new IncludeByRetentionIndexWindow(newTarget.retentionIndex, retentionIndexWindow)
    val massFilter = new IncludeByMassRange(newTarget, accurateMassWindow)
    val similarityFilter = new IncludeBySimilarity(newTarget, minimumSimilarity)

    val filteredByRi = unconfirmed.filter(riFilter.include(_, applicationContext))
    val filtedByMass = filteredByRi.filter(massFilter.include(_, applicationContext))
    val filteredBySimilarity = filtedByMass.filter(similarityFilter.include(_, applicationContext))

    logger.debug(s"existing targets: ${unconfirmed.size}")
    logger.debug(s"after ri filter: ${filteredByRi.size} targets are left")
    logger.debug(s"after mass filter: ${filtedByMass.size} targets are left")
    logger.debug(s"after similarity filter: ${filteredBySimilarity.size} targets are left")

    filteredBySimilarity.nonEmpty
  }
}
