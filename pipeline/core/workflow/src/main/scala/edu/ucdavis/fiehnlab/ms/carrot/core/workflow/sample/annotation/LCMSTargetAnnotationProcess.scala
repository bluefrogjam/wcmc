package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression, RetentionIndexDifference}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target, _}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import scala.collection.immutable.ListMap

/**
  * Created by wohlgemuth on 6/23/16.
  */
@Component
class LCMSTargetAnnotationProcess @Autowired()(val targets: LibraryAccess[Target], val lcmsProperties: LCMSTargetAnnotationProperties) extends AnnotationProcess[Target, CorrectedSample, AnnotatedSample](targets) with LazyLogging {

  /**
    * are we in debug mode, adds some sorting and prettifying for debug messages
    */
  lazy val debug: Boolean = logger.underlying.isDebugEnabled()


  //our defined filters to find possible matches are registered in here
  lazy val filters: SequentialAnnotate = new SequentialAnnotate(
    new MassAccuracyPPMorMD(5,lcmsProperties.massAccuracy, "annotation",lcmsProperties.massIntensity) ::
      new RetentionIndexAnnotation(lcmsProperties.retentionIndexWindow, "annotation") ::
      List()
  )

  /**
    * finds a match between the target and the sequence of spectra
    *
    * @param target
    * @param spectra
    * @return
    */
  def findMatchesForTarget(target: Target, spectra: Seq[_ <: Feature with CorrectedSpectra]): Seq[_ <: Feature with CorrectedSpectra] = {
    val result = spectra.collect {
      case spectra: Feature with CorrectedSpectra if filters.isMatch(spectra, target) =>
        spectra
    }

    if (debug) {
      logger.debug(s"discovered matches for ${target}")
      result.seq.sortBy(_.retentionIndex).foreach { ms =>
        logger.debug(s"\t=> ${ms}")
      }
    }
    result.seq
  }

  /**
    * removes duplicated target annotations and returns our decided match for this target
    *
    * @param target
    * @param matches
    */
  def removeDuplicatedTargetAnnotations(sample: CorrectedSample, target: Target, matches: Seq[_ <: Feature with CorrectedSpectra]): Option[Feature with CorrectedSpectra] = {

    if (matches.nonEmpty) {
      logger.debug(s"find best match for target $target, ${matches.size} possible annotations")

      val resultList =
        if (lcmsProperties.preferMassAccuracyOverRetentionIndexDistance) {
          logger.info("preferring accuracy over retention time distance")
          matches.sortBy { r => (MassAccuracy.calculateMassErrorPPM(r, target).get, RetentionIndexDifference.diff(target, r)) }
        }
        else {
          logger.info("preferring retention time over mass accuracy")
          matches.sortBy { r => (RetentionIndexDifference.diff(target, r), MassAccuracy.calculateMassErrorPPM(r, target).get) }
        }

      val best = resultList.head

      if (debug) {
        logger.debug("result list:")
        resultList.zipWithIndex.foreach { p =>
          logger.debug(s"\t\t=> ${p._1}")
          logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, ri distance was: ${RetentionIndexDifference.diff(target, p._1)}%1.3f, mass accuracy: ${MassAccuracy.calculateMassErrorPPM(p._1, target).get}%1.5f ")
        }
      }

      if (resultList.size == 1 || lcmsProperties.closePeakDetection == 0.0) {
        logger.debug(s"\t\t\t\t=>accepting ${best}")

        Some(best)
      }
      else {
        logger.debug(s"utilizing close peak detection mode since we have ${resultList.size} candidates")

        val closePeaks = resultList.sortBy(RetentionIndexDifference.diff(target, _))

        logger.debug(s"discovered close peaks after filtering: ${closePeaks}")
        if (closePeaks.nonEmpty) {
          if (debug) {
            logger.debug("close peaks:")
            closePeaks.zipWithIndex.foreach { p =>
              logger.debug(s"\t\t=> ${p._1}")
              logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, intensity was: ${p._1.massOfDetectedFeature.get.intensity}")
            }

            logger.debug(s"chosen: ${closePeaks.head}")
          }

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
    * associated with several targets and want to ensure we only has 1 annotation for each target
    *
    * @param possibleHits
    */
  def findUniqueAnnotationForTargets(possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)]): Map[Target, _ <: Feature with CorrectedSpectra] = {
    logger.debug(s"analyzing ${possibleHits.size} possible hits")
    //map all targets as list, indexed by it's corrected spectra
    val annotationsToTargets: Map[_ <: Feature with CorrectedSpectra, Seq[Target]] = possibleHits.groupBy(_._2).mapValues(_.map(_._1))

    //find the best possible hits for targets
    val hits = annotationsToTargets.map {

      x =>

        //only 1 annotation, so move forward
        if (x._2.size == 1) {
          logger.debug(s"only 1 target found for spectra: ${x._1}")
          (x._2.head, x._1)
        }
        //several annotations found, optimizing association
        else {
          logger.debug("")
          logger.debug(s"found ${x._2.size} targets for spectra ${x._1}")

          //find the target, which has the closest mass accuracy, followed by the closes retention time
          val optimizedTargetList =
            if (lcmsProperties.preferMassAccuracyOverRetentionIndexDistance) {
              logger.debug("preferring accuracy over retention time distance")
              x._2.sortBy(r => (MassAccuracy.calculateMassErrorPPM(x._1, r).get, RetentionIndexDifference.diff(r, x._1)))
            } else {
              logger.debug("preferring retention time over mass accuracy")
              x._2.sortBy(r => (RetentionIndexDifference.diff(r, x._1), MassAccuracy.calculateMassErrorPPM(x._1, r).get))
            }

          val hit = optimizedTargetList.head

          if (debug) {
            optimizedTargetList.zipWithIndex.foreach { y =>
              logger.debug(s"\t=>\t${y._1}")
              logger.debug(f"\t\t=> rank:                ${y._2}")
              logger.debug(f"\t\t=> ri distance:         ${RetentionIndexDifference.diff(y._1, x._1)}%1.2f seconds")
              logger.debug(f"\t\t=> mass error:          ${MassAccuracy.calculateMassError(x._1, y._1).get}%1.5f dalton")
              //              logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
              logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get}%1.5f ppm")
              logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassError(x._1, y._1).get * RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
              logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get / RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
              logger.debug(f"\t\t=> mass intensity:      ${x._1.massOfDetectedFeature.get.intensity}%1.0f")
              //              logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.intensity}%1.0f")


              logger.debug("")
            }

            logger.debug(s"best match: ${hit}")
          }

          if (lcmsProperties.closePeakDetection > 0.0) {
            logger.debug("utilizing close peak detection mode")

            val closePeaks = optimizedTargetList.filter { p => RetentionIndexDifference.diff(p, x._1) < lcmsProperties.closePeakDetection }.sortBy(p => RetentionIndexDifference.diff(p, x._1)).reverse

            if (closePeaks.nonEmpty) {

              logger.debug(s"analyzing close peaks, which are in a ${lcmsProperties.closePeakDetection}s window")
              closePeaks.zipWithIndex.foreach { y =>
                logger.debug(s"\t=>\t${y._1}")
                logger.debug(f"\t\t=> rank:                ${y._2}")
                logger.debug(f"\t\t=> ri distance:         ${RetentionIndexDifference.diff(y._1, x._1)}%1.2f seconds")
                logger.debug(f"\t\t=> mass error:          ${MassAccuracy.calculateMassError(x._1, y._1).get}%1.5f dalton")
                //                logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
                logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get}%1.5f ppm")
                logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassError(x._1, y._1).get * RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
                logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1).get / RetentionIndexDifference.diff(y._1, x._1)}%1.5f ppm")
                logger.debug(f"\t\t=> mass intensity:      ${x._1.massOfDetectedFeature.get.intensity}%1.0f")
                //                logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.intensity}%1.0f")


                logger.debug("")
              }

              val chosen = closePeaks.head
              logger.debug(s"chose target: ${chosen}")
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
    * processes all the spectra of the input sample against the provided library
    *
    * @param input
    * @return
    */
  override def process(input: CorrectedSample, targets: Iterable[Target]): AnnotatedSample = {
    logger.info(s"Annotating sample: ${input.name}")

    /**
      * internal recursive function to find all possible annotations in the sample
      *
      * @param spectra
      * @param targets
      */
    def annotate(input: CorrectedSample, spectra: Seq[_ <: Feature with CorrectedSpectra], targets: Iterable[Target]): Seq[(Target, _ <: Feature with CorrectedSpectra)] = {
      logger.info(s"sspectra requiring annotation: ${spectra.size}")
      val annotatedTargets: Map[Target, _ <: Feature with CorrectedSpectra] = if (debug) {
        //if debugging is enable, we sort by name
        ListMap(findMatchesForTargets(input, targets, spectra).toSeq.sortBy(_._1.name): _*)
      } else {
        //it's a bit faster this way
        findMatchesForTargets(input, targets, spectra)
      }

      //get all the targets, without an annotation
      val noneAnnotatedTargets: Iterable[Target] =
        if (debug) {
          //if debugging is enable, we sort by name
          targets.filter { target =>
            annotatedTargets.get(target).isEmpty
          }.toSeq.sortBy(_.name)
        }
        else {
          //it's a bit faster this way
          targets.filter { target =>
            annotatedTargets.get(target).isEmpty
          }
        }

      //get all none identified spectra
      val noneIdentifiedSpectra: Seq[_ <: Feature with CorrectedSpectra] = spectra.filter { s =>
        !annotatedTargets.values.exists { s2 =>
          s2.scanNumber == s.scanNumber && s2.massOfDetectedFeature == s.massOfDetectedFeature
        }
      }

      if (annotatedTargets.isEmpty) {
        List()
      }
      else {
        val firstResult = annotatedTargets.toSeq
        if (lcmsProperties.recursiveAnnotationMode) {
          logger.debug("utilizing recursive annotation model")
          val newResult = annotate(input, noneIdentifiedSpectra, noneAnnotatedTargets)

          List.concat(newResult, firstResult)
        }
        else {
          logger.debug("utilizing none recursive annotations mode")
          firstResult
        }
      }

    }

    val result = annotate(input, input.spectra, targets)


    //remap to correct types
    val annotatedSpectra: Seq[_ <: Feature with AnnotatedSpectra] = result.collect {

      case hit: (Target, Feature with CorrectedSpectra) =>

        SpectraHelper.addAnnotation(hit._2, MassAccuracy.calculateMassErrorPPM(hit._2, hit._1), MassAccuracy.calculateMassError(hit._2, hit._1), hit._1)
    }

    //find the none annotated spectra
    val noneAnnotatedSpectra: Seq[_ <: Feature with CorrectedSpectra] = input.spectra.filterNot { s =>
      annotatedSpectra.exists(x => x.scanNumber == s.scanNumber && x.massOfDetectedFeature == s.massOfDetectedFeature)
    }

    logger.debug(s"spectra count in sample ${input.spectra.size}")
    logger.debug(s"annotated spectra count: ${annotatedSpectra.size}")
    logger.debug(s"none annotated spectra count: ${noneAnnotatedSpectra.size}")

    new AnnotatedSample {

      override val spectra: Seq[_ <: Feature with AnnotatedSpectra with CorrectedSpectra] = annotatedSpectra
      override val correctedWith: Sample = input.correctedWith
      override val featuresUsedForCorrection: Seq[TargetAnnotation[Target, Feature]] = input.featuresUsedForCorrection
      override val regressionCurve: Regression = input.regressionCurve
      override val fileName: String = input.fileName
      override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = noneAnnotatedSpectra
    }
  }

  /**
    * tries to discover all the unique matches for the given target list for the given spectra
    *
    * @param targets
    * @param spectra
    * @return
    */
  def findMatchesForTargets(sample: CorrectedSample, targets: Iterable[Target], spectra: Seq[_ <: Feature with CorrectedSpectra]): Map[Target, _ <: Feature with CorrectedSpectra] = {
    logger.debug(s"defined targets: ${targets.size}")
    val possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)] = targets.toList.sortBy(_.retentionTimeInMinutes).collect {
      case target: Target =>
        val matches = findMatchesForTarget(target, spectra)

        logger.debug(s"found ${matches.size} matches for $target")


        removeDuplicatedTargetAnnotations(sample, target, matches) match {
          case x: Some[Feature with CorrectedSpectra] =>
            logger.debug(s"accepting")
            logger.debug(s"\t=>${x.get} as")
            logger.debug(s"\t\t=>${target}")

            (target, x.get)
          case None =>
        }

    }.collect {
      case x: (Target, Feature with CorrectedSpectra) => x
    }.seq

    logger.debug(s"found possible annotations: ${possibleHits.size}")
    //remove duplicates

    findUniqueAnnotationForTargets(possibleHits)

  }
}

@Component
@ConfigurationProperties(prefix = "annotation")
class LCMSTargetAnnotationProperties {

  /**
    * defined mass accuracy
    */
  @Value("${workflow.lcms.annotation.detection.massAccuracy:0.01}")
  var massAccuracy: Double = _

  /**
    * minimum intensity in percent the mass needs to have to be considered
    */
  var massIntensity: Float = 0f

  /**
    * the defined retention index window to use for it's given targets. It's considered in seconds
    */
  @Value("${workflow.lcms.annotation.detection.riWindow:5}")
  var retentionIndexWindow: Double = _

  /**
    * are we utilizing the recursive annotation mode. This means after an annotation run, we will utilize the left over targets and spectra and try to annotate these, until we have no hits left. This can be expensive computational wise and depending on settings
    * can annotate peaks wrongly
    */
  var recursiveAnnotationMode: Boolean = false

  /**
    * to decided the best peak, do we prefer mass accuracy or retention time distance difference
    *
    * by default we define the retention index to be more important due to isomeres.
    */
  @Value("${workflow.lcms.annotation.detection.massOverRI:false}")
  var preferMassAccuracyOverRetentionIndexDistance: Boolean = false

  /**
    * this enables the close peak detection system, if two possible targets are closer than n seconds, than the larger peak will be accepted as default annotation. Set to 0 to disable this feature
    */
  @Value("${workflow.lcms.annotation.detection.closePeak:3}")
  var closePeakDetection: Double = 3

}
