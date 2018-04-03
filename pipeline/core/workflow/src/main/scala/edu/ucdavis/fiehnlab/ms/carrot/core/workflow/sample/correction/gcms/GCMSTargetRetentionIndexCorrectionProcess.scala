package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{Regression, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Sample, Target, TargetAnnotation}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter._
import edu.ucdavis.fiehnlab.ms.carrot.math.CombinedRegression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[Target], val config: GCMSCorrectionLibraryProperties) extends CorrectionProcess(libraryAccess) with LazyLogging {
  override lazy val regression: Regression = new CombinedRegression(2,5)

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

    assert(method.chromatographicMethod.isDefined)
    logger.info(s"defined targets: ${targets.size}")

    val configuration = config.config.asScala.find(_.name == method.chromatographicMethod.get.name)

    configuration match {

      case Some(config) =>

        val includeByBasePeakFilter = new IncludesBasePeakSpectra(config.allowedBasePeaks.asScala, config.massAccuracy)
        val includeByIntensityFilter = new IncludesByPeakHeight(config.allowedBasePeaks.asScala, config.massAccuracy, config.minimumPeakIntensity)

        //filters all our spectra
        val msSpectra = input.spectra.collect {
          case x: MSSpectra => x
        }

        val spectraWithCorrectBasePeak = msSpectra.filter(includeByBasePeakFilter.include)
        val spectraWithCorrectionIntensity = spectraWithCorrectBasePeak.filter(includeByIntensityFilter.include)

        //find the target with the highest possible similarity match, to utilize it for distance ratio validation at a later point

        val distanceValidationTarget = targets.collect {
          case target: GCMSCorrectionTarget =>
            (target, spectraWithCorrectionIntensity.maxBy(x => Similarity.compute(x.asInstanceOf[SimilaritySupport], target)))
        }.maxBy(x => Similarity.compute(x._2.asInstanceOf[SimilaritySupport], x._1))

        //find potential targets
        val potentialMatches = targets.collect {
          case target: GCMSCorrectionTarget =>
            logger.info(s"evaluating target: ${target}")
            //check for similarity
            val similarity = new IncludeBySimilarity(target, target.config.minSimilarity)
            val ionRatios = new IncludeByIonRatio(target.config.qualifierIon, target.config.minQualifierRatio, target.config.maxQualifierRatio, config.massAccuracy)
            val distanceRatio = new IncludeByDistanceRatio(distanceValidationTarget._1, distanceValidationTarget._2, target, target.config.minDistanceRatio, target.config.maxDistanceRatio)

            val similarityMatches = spectraWithCorrectionIntensity.collect {
              case x: SimilaritySupport => x
            }.filter(similarity.include)

            //check for qualifier
            val ionRatioMatches = similarityMatches.filter(ionRatios.include)

            //validate by distance ratio
            val distanceRatioValidated = ionRatioMatches.filter(distanceRatio.include)

            //deal with double annotation
            if (distanceRatioValidated.size == 1) {
              TargetAnnotation(target.asInstanceOf[Target], distanceRatioValidated.head.asInstanceOf[Feature])
            }
            else if (distanceRatioValidated.size > 1) {
              //pick closest
              TargetAnnotation(target.asInstanceOf[Target], distanceRatioValidated.minBy(x => Math.abs(x.retentionTimeInSeconds - target.retentionIndex)).asInstanceOf[Feature])
            }
            else {
              //nothing found :(
              null
            }

        }.filter(_ != null)


        logger.info(s"matches: ${potentialMatches.size}")

        potentialMatches
      case None =>
        Seq.empty
    }


  }

  /**
    * minimum required count of standards found
    *
    * @return
    */
  override protected def getMinimumFoundStandards: Int = config.requiredStandards
}
