package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation.{AccurateMassAnnotation, RetentionIndexAnnotation, SequentialAnnotate}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression, RetentionTimeDifference}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Target, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import scala.collection.immutable.ListMap

/**
  * Created by wohlgemuth on 6/23/16.
  */
@Component
class LCMSTargetAnnotationProcess @Autowired()(val properties: WorkflowProperties, val targets: LibraryAccess[Target], val lcmsProperties: LCMSTargetAnnotationProperties) extends AnnotationProcess[Target, CorrectedSample, AnnotatedSample](targets, properties.trackChanges) with LazyLogging {

  /**
    * are we in debug mode, adds some sorting and prettifying for debug messages
    */
  lazy val debug: Boolean = logger.underlying.isDebugEnabled()


  //our defined filters to find possible matches are registered in here
  lazy val filters: SequentialAnnotate = new SequentialAnnotate(
    new AccurateMassAnnotation(lcmsProperties.massAccuracy / 1000, lcmsProperties.massIntensity) ::
    new RetentionIndexAnnotation(lcmsProperties.retentionIndexWindow) ::
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
      case spectra: Feature with CorrectedSpectra =>
        val result = filters.isMatch(spectra, target)

        if (result) {
          spectra
        }
    }.collect {
      case x: Feature with CorrectedSpectra => x
    }

    if (debug) {
      logger.debug(s"discovered matches for ${target}")
      result.seq.sortBy(_.retentionIndex).foreach { ms =>
        logger.debug(s"\t=> ${ms}")
      }
    }
    result
  }

  /**
    * removes duplicated target annotations and returns our decided match for this target
    *
    * @param target
    * @param matches
    */
  def removeDuplicatedTargetAnnotations(target: Target, matches: Seq[_ <: Feature with CorrectedSpectra]): Option[Feature with CorrectedSpectra] = {

    if (matches.nonEmpty) {
      logger.debug(s"find best match for target $target, ${matches.size} possible annotations")

      val resultList =
        if (lcmsProperties.preferMassAccuracyOverRetentionIndexDistance) {
          logger.debug("preferring accuracy over retention time distance")
          matches.sortBy { r => (MassAccuracy.calculateMassErrorPPM(r, target, lcmsProperties.massAccuracy / 1000).get, RetentionTimeDifference.inSeconds(target, r)) }
        }
        else {
          logger.debug("preferring retention time over mass accuracy")
          matches.sortBy { r => (RetentionTimeDifference.inSeconds(target, r), MassAccuracy.calculateMassErrorPPM(r, target, lcmsProperties.massAccuracy / 1000).get) }
        }

      val best = resultList.head

      if (debug) {
        resultList.zipWithIndex.foreach { p =>
          logger.debug(s"\t\t=> ${p._1}")
          logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, ri distance was: ${RetentionTimeDifference.inSeconds(target, p._1)}%1.3f, mass accuracy: ${MassAccuracy.calculateMassErrorPPM(p._1, target, lcmsProperties.massAccuracy / 1000).get}%1.5f ")
        }
      }

      if (resultList.size == 1 || lcmsProperties.closePeakDetection == 0.0) {
        logger.debug(s"\t\t\t\t=>accepting ${best}")
        Some(best)
      }
      else {
        logger.debug(s"utilizing close peak detection mode, since retention time difference was less than ${lcmsProperties.closePeakDetection}")

        val closePeaks = resultList.filter(p => RetentionTimeDifference.inSeconds(target, p) < lcmsProperties.closePeakDetection).sortBy { ms =>
          val ion = MassAccuracy.findClosestIon(ms, target.monoIsotopicMass.get /*, lcmsProperties.massAccuracy*/)

          if (ion.isDefined) {
            ion.get.intensity
          }
          else {
            0.0f
          }
        }.reverse

        if (closePeaks.nonEmpty) {
          if (debug) {
            closePeaks.zipWithIndex.foreach { p =>
              logger.debug(s"\t\t=> ${p._1}")
              val closestIon = MassAccuracy.findClosestIon(p._1, target.monoIsotopicMass.get /*, lcmsProperties.massAccuracy*/).get

              logger.debug(f"\t\t\t=> rank: ${p._2 + 1}, intensity was: ${closestIon.intensity}")
            }
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
    val hits = annotationsToTargets /*.par */ .map {

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
              x._2.sortBy(r => (MassAccuracy.calculateMassErrorPPM(x._1, r, lcmsProperties.massAccuracy / 1000).get, RetentionTimeDifference.inSeconds(r, x._1)))
            } else {
              logger.debug("preferring retention time over mass accuracy")
              x._2.sortBy(r => (RetentionTimeDifference.inSeconds(r, x._1), MassAccuracy.calculateMassErrorPPM(x._1, r, lcmsProperties.massAccuracy / 1000).get))
            }

          val hit = optimizedTargetList.head

          if (debug) {
            optimizedTargetList.zipWithIndex.foreach { y =>
              logger.debug(s"\t=>\t${y._1}")
              logger.debug(f"\t\t=> rank:                ${y._2}")
              logger.debug(f"\t\t=> ri distance:         ${RetentionTimeDifference.inSeconds(y._1, x._1)}%1.2f seconds")
              logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get).get.mass)}%1.5f dalton")
              //              logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
              logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get}%1.5f ppm")
              logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get * RetentionTimeDifference.inSeconds(y._1, x._1)}%1.5f ppm")
              logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get / RetentionTimeDifference.inSeconds(y._1, x._1)}%1.5f ppm")
              logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get).get.intensity}%1.0f")
              //              logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.intensity}%1.0f")


              logger.debug("")
            }

            logger.debug(s"best match: ${hit}")
          }

          if (lcmsProperties.closePeakDetection > 0.0) {
            logger.debug("utilizing close peak detection mode")

            val closePeaks = optimizedTargetList.filter { p => RetentionTimeDifference.inSeconds(p, x._1) < lcmsProperties.closePeakDetection }.sortBy(target => MassAccuracy.findClosestIon(x._1, target.monoIsotopicMass.get /*, lcmsProperties.massAccuracy*/).get.intensity).reverse

            if (closePeaks.nonEmpty) {

              logger.debug(s"analyzing close peaks, which are in a ${lcmsProperties.closePeakDetection}s window")
              closePeaks.zipWithIndex.foreach { y =>
                logger.debug(s"\t=>\t${y._1}")
                logger.debug(f"\t\t=> rank:                ${y._2}")
                logger.debug(f"\t\t=> ri distance:         ${RetentionTimeDifference.inSeconds(y._1, x._1)}%1.2f seconds")
                logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get).get.mass)}%1.5f dalton")
                //                logger.debug(f"\t\t=> mass error:          ${Math.abs(y._1.monoIsotopicMass.get - MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get, lcmsProperties.massAccuracy / 1000).get.mass)}%1.5f dalton")
                logger.debug(f"\t\t=> mass error (ppm):    ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get}%1.5f ppm")
                logger.debug(f"\t\t=> mass error * ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get * RetentionTimeDifference.inSeconds(y._1, x._1)}%1.5f ppm")
                logger.debug(f"\t\t=> mass error / ri dis: ${MassAccuracy.calculateMassErrorPPM(x._1, y._1, lcmsProperties.massAccuracy / 1000).get / RetentionTimeDifference.inSeconds(y._1, x._1)}%1.5f ppm")
                logger.debug(f"\t\t=> mass intensity:      ${MassAccuracy.findClosestIon(x._1, y._1.monoIsotopicMass.get).get.intensity}%1.0f")
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


    /**
      * internal recursive function to find all possible annotations in the sample
      *
      * @param spectra
      * @param targets
      */
    def annotate(spectra: Seq[_ <: Feature with CorrectedSpectra], targets: Iterable[Target]): Seq[(Target, _ <: Feature with CorrectedSpectra)] = {

      val annotatedTargets: Map[Target, _ <: Feature with CorrectedSpectra] = if (debug) {
        //if debugging is enable, we sort by name
        ListMap(findMatchesForTargets(targets, spectra).toSeq.sortBy(_._1.name): _*)
      } else {
        //it's a bit faster this way
        findMatchesForTargets(targets, spectra)
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
          val newResult = annotate(noneIdentifiedSpectra, noneAnnotatedTargets)

          List.concat(newResult, firstResult)
        }
        else {
          logger.debug("utilizing none recursive annotations mode")
          firstResult
        }
      }

    }

    val result = annotate(input.spectra, targets)


    //remap to correct types
    val annotatedSpectra: Seq[_ <: Feature with AnnotatedSpectra] = result.collect {

      case hit: (Target, Feature with CorrectedSpectra) =>

        SpectraHelper.addAnnotation(hit._2,MassAccuracy.calculateMassErrorPPM(hit._2, hit._1, lcmsProperties.massAccuracy), MassAccuracy.calculateMassError(hit._2, hit._1, lcmsProperties.massAccuracy),hit._1)
    }

    //find the none annotated spectra
    val noneAnnotatedSpectra: Seq[_ <: Feature with CorrectedSpectra] = input.spectra.filterNot { s =>
      annotatedSpectra.exists( x => x.scanNumber == s.scanNumber && x.massOfDetectedFeature == s.massOfDetectedFeature )
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
  def findMatchesForTargets(targets: Iterable[Target], spectra: Seq[_ <: Feature with CorrectedSpectra]): Map[Target, _ <: Feature with CorrectedSpectra] = {
    logger.debug(s"defined targets: ${targets.size}")
    val possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)] = targets.toList.sortBy(_.retentionTimeInMinutes).par.collect {
      case target: Target =>
        val matches = findMatchesForTarget(target, spectra)

        logger.debug(s"found ${matches.size} matches for $target")


        removeDuplicatedTargetAnnotations(target, matches) match {
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
    * defined mass accuracy in milli dalton for the annotation process
    */
  @Value("${lcmsProperties.massAccuracy:15}")
  var massAccuracy: Double = _

  /**
    * minimum intensity in percent the mass needs to have to be considered
    */
  var massIntensity: Float = 0f

  /**
    * the defined retention index window to use for it's given targets. It's considered in seconds
    */
  @Value("${lcmsProperties.retentionIndexWindow:12}")
  var retentionIndexWindow: Double = _

  /**
    * are we utilizing the recursive annotation mode. This means after an annotation run, we will utilize the left over targets and spectra and try to annotate these, until we have no hits left. This can be expensive computational wise and depending on settings
    * can annotate peaks wrongly
    */
  var recursiveAnnotationMode: Boolean = false

  /**
    * to decided the best peak, do we prefer mass accuracy or retention time distance difference
    */
  var preferMassAccuracyOverRetentionIndexDistance: Boolean = true

  /**
    * this enables the close peak detection system, if two possible targets are closer than n seconds, than the larger peak will be accepted as default annotation. Set to 0 to disable this feature
    */
  var closePeakDetection: Double = 3

}
