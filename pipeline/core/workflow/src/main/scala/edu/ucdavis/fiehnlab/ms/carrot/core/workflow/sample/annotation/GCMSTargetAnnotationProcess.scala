package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONTargetLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.RetentionIndexDifference
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotateSampleProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByRetentionIndexWindow, SifterFilter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.annotation.GCMSAnnotationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetAnnotationProcess @Autowired()(val targets: LibraryAccess[Target], val gcmsPropterties: GCMSAnnotationProperties) extends AnnotateSampleProcess(targets) with LazyLogging {

  /**
    * should a recruce annotation mode be used
    */
  override protected val recursiveAnnotationMode: Boolean = false

  /**
    * this method is utilized to find the initial hits for the given target
    *
    * @param target
    * @param spectra
    * @param sample
    * @return
    */
  override protected def findMatches(target: Target, spectra: Seq[_ <: Feature with CorrectedSpectra], sample: CorrectedSample, method: AcquisitionMethod): Seq[_ <: Feature with CorrectedSpectra] = {

    val configuration = gcmsPropterties.config.asScala.find(_.column == method.chromatographicMethod.column.get)

    if (configuration.isEmpty) {
      throw new Exception(s"please ensure you are provided a valid method for this process! ${method} vs ${gcmsPropterties}")
    }

    trait TargetToLog extends JSONTargetLogging {
      /**
        * which target we require to log
        */
      override protected val targetToLog: Target = target
    }

    val retentionIndexFiler = new IncludeByRetentionIndexWindow(target.retentionIndex, "annotation", configuration.get.retentionIndexWindow) with TargetToLog
    val sifterFilter = new SifterFilter("annotation", configuration.get, target)

    //filter spectra

    //logger.info(s"${input.spectra.size} to annotate vs ${target}")

    //by unique mass
    //by retention index
    val filteredByRi = spectra.filter(retentionIndexFiler.include(_, applicationContext))

    //logger.info(s"${filteredByRi.size} to annotate now")
    val filteredBySifter = filteredByRi.filter(x => sifterFilter.include(x.asInstanceOf[MSSpectra], applicationContext))

    filteredBySifter
  }

  /**
    * this removes duplicated target annotations
    *
    * @param sample
    * @param target
    * @param matches
    * @return
    */
  override protected def removeDuplicatedTargets(sample: CorrectedSample, target: Target, matches: Seq[_ <: Feature with CorrectedSpectra], method: AcquisitionMethod): Option[Feature with CorrectedSpectra] = {
    if (matches.nonEmpty) {
      logger.debug(s"find best match for target $target, ${matches.size} possible annotations")

      val closePeaks = matches.sortBy(RetentionIndexDifference.diff(target, _))

      logger.debug(s"discovered close peaks after filtering: ${closePeaks}")
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
      None
    }
  }

  /**
    * utilized to find unique annotations
    *
    * @param possibleHits
    * @return
    */
  override protected def removeDuplicatedAnnotations(possibleHits: Seq[Tuple2[Target, _ <: Feature with CorrectedSpectra]], method: AcquisitionMethod): Map[Target, _ <: Feature with CorrectedSpectra] = {
    logger.debug(s"analyzing ${possibleHits.size} possible hits")
    //map all targets as list, indexed by it's corrected spectra
    val annotationsToTargets: Map[_ <: Feature with CorrectedSpectra, Seq[Target]] = possibleHits.groupBy(_._2).mapValues(_.map(_._1))

    //find the best possible hits for targets
    val hits = annotationsToTargets.par.collect {

      case x if x._2.nonEmpty =>

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
          val optimizedTargetList = x._2.sortBy(r => RetentionIndexDifference.diff(r, x._1))


          val hit = optimizedTargetList.head

          if (debug) {
            optimizedTargetList.zipWithIndex.foreach { y =>
              logger.debug(s"\t=>\t${y._1}")
              logger.debug(f"\t\t=> rank:                ${y._2}")
              logger.debug(f"\t\t=> ri distance:         ${RetentionIndexDifference.diff(y._1, x._1)}%1.2f seconds")
              logger.debug(f"\t\t=> mass intensity:      ${x._1.massOfDetectedFeature.get.intensity}%1.0f")
              logger.debug("")
            }

            logger.debug(s"best match: ${hit}")
          }

          (hit, x._1)
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
}
