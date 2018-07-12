package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.immutable.ListMap

/**
  * this is an abstract base class for all annotation based sub processes
  *
  * @param libraryAccess
  */
abstract class AnnotateSampleProcess[T <: Target] @Autowired()(val libraryAccess: LibraryAccess[T], stasisClient: StasisService) extends AnnotationProcess[T, CorrectedSample, AnnotatedSample](libraryAccess, stasisClient) with LazyLogging {

  /**
    * are we in debug mode, adds some sorting and prettifying for debug messages
    */
  lazy val debug: Boolean = logger.underlying.isDebugEnabled()

  /**
    * should a recruce annotation mode be used
    */
  protected val recursiveAnnotationMode: Boolean

  /**
    * processes all the spectra of the input sample against the provided library
    *
    * @param input
    * @return
    */
  final override def process(input: CorrectedSample, targets: Iterable[T], method: AcquisitionMethod): AnnotatedSample = {
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
        ListMap(findMatchesForTargets(input, targets, spectra,method).toSeq.sortBy(_._1.name): _*)
      } else {
        //it's a bit faster this way
        findMatchesForTargets(input, targets, spectra,method)
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
        if (recursiveAnnotationMode) {
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

    val annotatedSample = new AnnotatedSample {

      override val spectra: Seq[_ <: Feature with AnnotatedSpectra with CorrectedSpectra] = annotatedSpectra
      override val correctedWith: Sample = input.correctedWith
      override val featuresUsedForCorrection: Iterable[TargetAnnotation[Target, Feature]] = input.featuresUsedForCorrection
      override val regressionCurve: Regression = input.regressionCurve
      override val fileName: String = input.fileName
      override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = noneAnnotatedSpectra
      /**
        * associated properties
        */
      override val properties: Option[SampleProperties] = input.properties
    }

    stasisClient.addTracking(TrackingData(annotatedSample.name, "annotated", annotatedSample.fileName))
    annotatedSample
  }

  /**
    * this method is utilized to find the initial hits for the given target
    *
    * @param target
    * @param spectra
    * @param sample
    * @return
    */
  protected def findMatches(target: Target, spectra: Seq[_ <: Feature with CorrectedSpectra], sample: CorrectedSample,method: AcquisitionMethod): Seq[_ <: Feature with CorrectedSpectra]

  /**
    * this removes duplicated target annotations
    *
    * @param sample
    * @param target
    * @param matches
    * @return
    */
  protected def removeDuplicatedTargets(sample: CorrectedSample, target: Target, matches: Seq[_ <: Feature with CorrectedSpectra],method: AcquisitionMethod): Option[Feature with CorrectedSpectra]


  /**
    * utilized to find unique annotations
    *
    * @param possibleHits
    * @return
    */
  protected def removeDuplicatedAnnotations(possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)],method: AcquisitionMethod): Map[Target, _ <: Feature with CorrectedSpectra]

  /**
    * tries to discover all the unique matches for the given target list for the given spectra
    *
    * @param targets
    * @param spectra
    * @return
    */
  private def findMatchesForTargets(sample: CorrectedSample, targets: Iterable[Target], spectra: Seq[_ <: Feature with CorrectedSpectra],method: AcquisitionMethod): Map[Target, _ <: Feature with CorrectedSpectra] = {
    logger.debug(s"defined targets: ${targets.size}")
    val possibleHits: Seq[(Target, _ <: Feature with CorrectedSpectra)] = targets.toList.sortBy(_.retentionTimeInMinutes).par.collect {
      case target: Target =>
        val matches = findMatches(target, spectra, sample,method)

        logger.debug(s"found ${matches.size} matches for $target")


        removeDuplicatedTargets(sample, target, matches,method) match {
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

    removeDuplicatedAnnotations(possibleHits,method)

  }
}
