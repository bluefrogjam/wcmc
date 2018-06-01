package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.{JSONSampleLogging, JSONTargetLogging}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{Regression, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target, TargetAnnotation}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.correction.{GCMSCorrectionLibraryProperties, GCMSCorrectionTarget, GCMSLibraryConfiguration}
import edu.ucdavis.fiehnlab.ms.carrot.math.CombinedRegression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[Target], val config: GCMSCorrectionLibraryProperties) extends CorrectionProcess(libraryAccess) with LazyLogging {
  override lazy val regression: Regression = new CombinedRegression(2, 5)

  /**
    * abstract method to acutall find out targets
    * required for the correction
    * of this sample
    *
    * @param input
    * @param targets
    * @return
    */
  override protected def findCorrectionTargets(input: Sample, targets: Iterable[Target], method: AcquisitionMethod): Iterable[TargetAnnotation[Target, Feature]] = {

    logger.info(s"defined targets: ${targets.size}")

    val configuration = config.config.asScala.find(_.name == method.chromatographicMethod.name)

    configuration match {

      case Some(config) =>

        val includeByBasePeakFilter = new IncludesBasePeakSpectra(config.allowedBasePeaks.asScala, "correction", config.massAccuracy) with JSONSampleLogging {
          /**
            * which sample we require to log
            */
          override protected val sampleToLog: String = input.name
        }

        val includeByIntensityFilter = new IncludeByTicFilter(config.minimumPeakIntensity,"correction") with JSONSampleLogging {
          /**
            * which sample we require to log
            */
          override protected val sampleToLog: String = input.name
        }


        //filters all our spectra
        val msSpectra = input.spectra.collect {
          case x: MSSpectra => x
        }

        val spectraWithCorrectBasePeak = msSpectra.filter(includeByBasePeakFilter.include(_, applicationContext))
        val spectraWithCorrectionIntensity = spectraWithCorrectBasePeak.filter(includeByIntensityFilter.include(_, applicationContext))

        logger.info("searching for validation target")
        //find the target with the highest possible similarity match, to utilize it for distance ratio validation at a later point
        val distanceValidationTargets = targets.par.collect {
          case target: GCMSCorrectionTarget if target.config.validationTarget =>
            findMatchToTarget(config, spectraWithCorrectionIntensity, None, target, input)
        }.filter(_ != null)

        if (distanceValidationTargets.isEmpty) {
          throw new NotEnoughStandardsFoundException(s"${input.name} was not able to find a validation target!")
        }

        //take the top 3 similarity wise and than the largest peak
        val distanceValidationTarget = distanceValidationTargets.toSeq.seq.sortBy { x =>
          val similarity = Similarity.compute(x.annotation.asInstanceOf[SimilaritySupport], x.target)
          //logger.info(s"similarity for ${x.target.name} is ${similarity}")
          similarity
        }.reverse.slice(0, 3).maxBy(x => {
          val max = x.annotation.associatedScan.get.basePeak.intensity

          //logger.info(s"max for ${x.target.name} is ${max}")
          max
        })

        logger.info(s"using ${distanceValidationTarget.target.name} for validation purposes")


        //find potential targets
        val potentialMatches = targets.par.collect {
          case target: GCMSCorrectionTarget =>
            findMatchToTarget(config, spectraWithCorrectionIntensity, Option(distanceValidationTarget), target, input)
        }.filter(_ != null)


        logger.debug(s"potential matches: ${potentialMatches.size}")

        potentialMatches.seq
      case None =>
        Seq.empty
    }


  }

  /**
    * finds a match to a target, with a possible validation target
    *
    * @param config
    * @param spectraWithCorrectionIntensity
    * @param distanceValidationTarget
    * @param target
    * @return
    */
  private def findMatchToTarget(config: GCMSLibraryConfiguration, spectraWithCorrectionIntensity: Seq[MSSpectra], distanceValidationTarget: Option[TargetAnnotation[Target, Feature]], target: GCMSCorrectionTarget, input: Sample): TargetAnnotation[Target, Feature] = {
    logger.info(s"evaluating target: ${target.name.get}")
    //check for similarity
    val similarity = new IncludeBySimilarity(target, target.config.minSimilarity, "correction") with JSONSampleLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.name
    }

    val ionRatios = new IncludeByIonRatio(target.config.qualifierIon, target.config.minQualifierRatio, target.config.maxQualifierRatio, "correction", config.massAccuracy) with JSONSampleLogging with JSONTargetLogging {
      /**
        * which sample we require to log
        */
      override protected val sampleToLog: String = input.name
      /**
        * which target we require to log
        */
      override protected val targetToLog: Target = target
    }


    val similarityMatches = spectraWithCorrectionIntensity.collect {
      case x: SimilaritySupport => x
    }.filter(similarity.include(_, applicationContext))

    if (similarityMatches.isEmpty) {
      logger.warn(s"no matches found for IncludeBySimilarity, reconsider your configuration for target: ${target.name}/${target.retentionIndex}")
    }
    //check for qualifier
    val ionRatioMatches = similarityMatches.filter(ionRatios.include(_, applicationContext))

    logger.info(s"ion ratio matches: ${ionRatioMatches.size}")
    //validate by distance ratio

    if (ionRatioMatches.isEmpty) {
      logger.warn(s"no matches found for IncludeByIonRatio, reconsider your configuration for target: ${target.name}/${target.retentionIndex}")
    }

    val validated = if (distanceValidationTarget.isDefined) {
      val distanceRatioValidated = target.config.distanceRatios.asScala.map { x =>
        ionRatioMatches.filter(new IncludeByDistanceRatio(distanceValidationTarget.get.target, distanceValidationTarget.get.annotation, target, x.min, x.max, "correction") with JSONSampleLogging {
          /**
            * which sample we require to log
            */
          override protected val sampleToLog: String = input.name
        }
            .include(_, applicationContext))
      }.filter(_.nonEmpty).sortBy(_.size).flatten

      if (distanceRatioValidated.isEmpty) {
        logger.warn(s"no matches found for IncludeByDistanceRatio, reconsider your configuration for target: ${target.name}/${target.retentionIndex}")
        Seq.empty
      }

      distanceRatioValidated
    }
    else {
      ionRatioMatches
    }

    logger.info(s"validated matches: ${validated.size}")
    //deal with double annotation
    if (validated.size == 1) {
      TargetAnnotation(target.asInstanceOf[Target], validated.head.asInstanceOf[Feature])
    }
    else if (validated.size > 1) {
      logger.info("more than 1 possible annotation found")
      validated.foreach { x =>
        logger.info(s"annotaton: ${x}")
      }

      val best = validated.minBy(x => Math.abs(x.retentionTimeInSeconds - target.retentionIndex)).asInstanceOf[Feature]
      logger.info(s"picked: ${best}")
      //pick closest
      TargetAnnotation(target.asInstanceOf[Target], best)
    }
    else {
      //nothing found :(
      null
    }
  }

  /**
    * minimum required count of standards found
    *
    * @return
    */
  override protected def getMinimumFoundStandards: Int = config.requiredStandards
}
