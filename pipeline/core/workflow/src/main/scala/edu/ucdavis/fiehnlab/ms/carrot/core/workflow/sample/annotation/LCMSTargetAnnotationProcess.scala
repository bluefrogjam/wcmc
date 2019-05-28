package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, RetentionIndexDifference}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotateSampleProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target, _}
import edu.ucdavis.fiehnlab.ms.carrot.math.SimilarityMethods
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.beans.BeanProperty
import scala.collection.immutable.ListMap

/**
  * Created by wohlgemuth on 6/23/16.
  */
@Component
@Profile(Array("carrot.lcms"))
class LCMSTargetAnnotationProcess @Autowired()(val targets: MergeLibraryAccess, val lcmsProperties: LCMSAnnotationProcessProperties, stasisClient: StasisService) extends AnnotateSampleProcess(targets, stasisClient) with Logging {

  /**
    * finds a match between the target and the sequence of spectra
    *
    * @param target
    * @param spectra
    * @return
    */
  override protected def findMatches(target: Target, spectra: Seq[_ <: Feature with CorrectedSpectra], sample: CorrectedSample, method: AcquisitionMethod): Seq[_ <: Feature with CorrectedSpectra] = {
    val filters: SequentialAnnotate = new SequentialAnnotate(
      new MassAccuracyDalton(lcmsProperties.massAccuracySetting,
        lcmsProperties.massIntensity) ::
        new RetentionIndexAnnotation(lcmsProperties.retentionIndexWindow) ::
        List()
    )


    val result = spectra.collect {
      case spectra: Feature with CorrectedSpectra if filters.isMatch(spectra, target) =>
        spectra
    }

    result.seq
  }

  /**
    * removes duplicated target annotations and returns our decided match for this target
    *
    * @param target
    * @param matches
    */
  protected override def removeDuplicatedTargets(sample: CorrectedSample, target: Target, matches: Seq[_ <: Feature with CorrectedSpectra], method: AcquisitionMethod): Option[Feature with CorrectedSpectra] = {

    if (matches.nonEmpty) {
      logger.debug(s"find best match for target $target, ${matches.size} possible annotations")

      val resultList = if (matches.size == 1) {
        matches
      } else {
        if (lcmsProperties.preferGaussianSimilarityForAnnotation) {
          logger.debug("preferring gaussian similarity over mass accuracy and rt distance")
          matches.sortBy { r => (MassAccuracy.calculateMassError(r, target).get, SimilarityMethods.featureTargetSimilarity(r, target, lcmsProperties.massAccuracySetting, lcmsProperties.retentionIndexWindow, lcmsProperties.intensityPenaltyThreshold)) }

        } else if (lcmsProperties.preferMassAccuracyOverRetentionIndexDistance) {
          logger.debug("preferring accuracy over retention time distance")
          matches.sortBy { r => (MassAccuracy.calculateMassErrorPPM(r, target).get, RetentionIndexDifference.diff(target, r)) }
        } else {
          logger.debug("preferring retention time over mass accuracy")
          matches.sortBy { r => (RetentionIndexDifference.diff(target, r), MassAccuracy.calculateMassErrorPPM(r, target).get) }
        }
      }

      val best = resultList.head

      //        logger.debug("result list:")
      //        resultList.zipWithIndex.foreach { p =>
      //          logger.debug(s"\t\t=> ${p._1}")
      //          logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, ri distance was: ${RetentionIndexDifference.diff(target, p._1)}%1.3f, mass accuracy: ${MassAccuracy.calculateMassErrorPPM(p._1, target).get}%1.5f ")
      //        }

      if (resultList.size == 1 || lcmsProperties.closePeakDetection == 0.0) {
        logger.debug(s"\t\t\t\t=>accepting ${best}")

        Some(best)
      }
      else {
        logger.info(s"utilizing close peak detection mode since we have ${resultList.size} candidates")

        val closePeaks = resultList.sortBy(RetentionIndexDifference.diff(target, _))

        logger.info(s"discovered close peaks after filtering: ${closePeaks}")
        if (closePeaks.nonEmpty) {
          //          if (debug) {
          //            logger.debug("close peaks:")
          //            closePeaks.zipWithIndex.foreach { p =>
          //              logger.debug(s"\t\t=> ${p._1}")
          //              logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, intensity was: ${p._1.massOfDetectedFeature.get.intensity}")
          //            }
          //
          //            logger.debug(s"chosen: ${closePeaks.head}")
          //          }

          Some(closePeaks.head)
        }
        else {
          logger.debug("no close peak detected")
          Some(best)
        }
      }
    }
    else {
      None
    }
  }

  /**
    * finds the best possible annotation of the sequence of targets and spectra. Basically we can have the same spectra
    * associated with several targets and want to ensure we only have 1 annotation for each target
    *
    * @param possibleHits
    */
  protected override def removeDuplicatedAnnotations(possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)], method: AcquisitionMethod): Map[Target, _ <: Feature with CorrectedSpectra] = {
    //map all targets as list, indexed by it's corrected spectra
    val annotationsToTargets: Map[_ <: Feature with CorrectedSpectra, Seq[Target]] = possibleHits.groupBy(_._2).mapValues(_.map(_._1))

    //find the best possible hits for targets
    val hits = annotationsToTargets.map {

      x =>

        //only 1 annotation, so move forward
        if (x._2.size == 1) {
          (x._2.head, x._1)
        }
        //several annotations found, optimizing association
        else {

          //find the target, which has the closest mass accuracy, followed by the closes retention time
          val optimizedTargetList =
            if (lcmsProperties.preferGaussianSimilarityForAnnotation) {
              logger.debug("preferring gaussian similarity over mass accuracy and rt distance")
              x._2.sortBy { r => (MassAccuracy.calculateMassError(x._1, r), SimilarityMethods.featureTargetSimilarity(x._1, r, lcmsProperties.massAccuracySetting, lcmsProperties.retentionIndexWindow, lcmsProperties.intensityPenaltyThreshold)) }
            } else if (lcmsProperties.preferMassAccuracyOverRetentionIndexDistance) {
              logger.debug("preferring accuracy over retention time distance")
              x._2.sortBy(r => (MassAccuracy.calculateMassErrorPPM(x._1, r).get, RetentionIndexDifference.diff(r, x._1)))
            } else {
              logger.debug("preferring retention time over mass accuracy")
              x._2.sortBy(r => (RetentionIndexDifference.diff(r, x._1), MassAccuracy.calculateMassErrorPPM(x._1, r).get))
            }

          val hit = optimizedTargetList.head

          //          if (debug) {
          //            optimizedTargetList.zipWithIndex.foreach { y =>
          //              logger.debug(s"\t=>\t${y._1}")
          //              logger.debug(f"\t\t=> rank:                ${y._2}")
          //              logger.debug(f"\t\t=> ri distance:         ${RetentionIndexDifference.diff(y._1, x._1)}%1.2f seconds")
          //              logger.debug(f"\t\t=> mass error:          ${MassAccuracy.calculateMassError(x._1, y._1).get}%1.5f dalton")
          //              //              logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
          //              logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get}%1.5f ppm")
          //              logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassError(x._1, y._1).get * RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
          //              logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get / RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
          //              logger.debug(f"\t\t=> mass intensity:      ${x._1.massOfDetectedFeature.get.intensity}%1.0f")
          //              //              logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.intensity}%1.0f")
          //
          //
          //              logger.debug("")
          //            }
          //
          //            logger.debug(s"best match: ${hit}")
          //          }

          if (lcmsProperties.closePeakDetection > 0.0) {

            val closePeaks = optimizedTargetList.filter { p => RetentionIndexDifference.diff(p, x._1) < lcmsProperties.closePeakDetection }.sortBy(p => RetentionIndexDifference.diff(p, x._1)) //.reverse

            if (closePeaks.nonEmpty) {

              //              logger.debug(s"analyzing close peaks, which are in a ${lcmsProperties.closePeakDetection}s window")
              //              closePeaks.zipWithIndex.foreach { y =>
              //                logger.debug(s"\t=>\t${y._1}")
              //                logger.debug(f"\t\t=> rank:                ${y._2}")
              //                logger.debug(f"\t\t=> ri distance:         ${RetentionIndexDifference.diff(y._1, x._1)}%1.2f seconds")
              //                logger.debug(f"\t\t=> mass error:          ${MassAccuracy.calculateMassError(x._1, y._1).get}%1.5f dalton")
              // logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
              //                logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get}%1.5f ppm")
              //                logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassError(x._1, y._1).get * RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
              //                logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get / RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
              //                logger.debug(f"\t\t=> mass intensity:      ${x._1.massOfDetectedFeature.get.intensity}%1.0f")
              // logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.intensity}%1.0f")


              //                logger.debug("")
              //              }

              val chosen = closePeaks.head
              //              logger.debug(s"chose target: ${chosen}")
              (chosen, x._1)
            }
            else {
              (hit, x._1)
            }
          } else {
            (hit, x._1)
          }
        }
    }.seq

    if (debug) {
      val result = ListMap(hits.toSeq.sortBy(_._1.name): _*)
      logger.debug(s"\t=> ${result.size} annotations found after optimization")
      result
    }
    else {
      hits
    }
  }

  /**
    * should a recursive annotation mode be used
    */
  lazy override protected val recursiveAnnotationMode: Boolean = lcmsProperties.recursiveAnnotationMode
}

@Component
@Profile(Array("carrot.lcms"))
@ConfigurationProperties(prefix = "wcmc.workflow.lcms.process.annotation")
class LCMSAnnotationProcessProperties {

  /**
    * minimum intensity in absolute counts the mass needs to have to be considered
    */
  @BeanProperty
  var massIntensity: Float = 0

  /**
    * the defined retention index window to use for it's given targets. It's considered in seconds
    * should be half of the intended window
    */
  @BeanProperty
  var retentionIndexWindow: Double = 6

  /**
    * are we utilizing the recursive annotation mode.
    * This means after an annotation run, we will utilize the left over targets and spectra and try to annotate these,
    * until we have no hits left.
    * This can be expensive computational wise and depending on settings
    * Can annotate peaks wrongly
    */
  @BeanProperty
  var recursiveAnnotationMode: Boolean = false

  /**
    * to decided the best peak, do we prefer mass accuracy or retention time distance difference
    *
    * by default we define the retention index to be more important due to isomeres.
    */
  @BeanProperty
  var preferMassAccuracyOverRetentionIndexDistance: Boolean = false

  /**
    * to decided the best peak, do we prefer mass accuracy or retention time distance difference
    *
    * by default we define the retention index to be more important due to isomeres.
    */
  @BeanProperty
  var preferGaussianSimilarityForAnnotation: Boolean = true

  /**
    * this enables the close peak detection system, if two possible targets are closer than n seconds,
    * then the larger peak will be accepted as default annotation. Set to 0 to disable this feature
    */
  @BeanProperty
  var closePeakDetection: Double = 3


  /**
    * Mass accuracy (in Dalton) used in target filtering and similarity calculation
    */
  @BeanProperty
  var massAccuracySetting: Double = 0.010

  /**
    * Mass accuracy (in PPM) used in target filtering and similarity calculation
    */
  @BeanProperty
  var massAccuracySettingPpm: Double = 10

  /**
    * Intensity used for penalty calculation - the peak similarity score for targets below this
    * intensity will be scaled down by the ratio of the intensity to this threshold
    */
  @BeanProperty
  var intensityPenaltyThreshold: Float = 1000


}
