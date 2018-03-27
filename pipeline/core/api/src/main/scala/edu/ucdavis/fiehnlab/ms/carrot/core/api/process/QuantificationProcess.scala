package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSpectra, Sample, Target, _}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * quantifies a sample, so it's ready to be exported
  */

abstract class QuantificationProcess[T](libraryAccess: LibraryAccess[Target]) extends AnnotationProcess[Target, AnnotatedSample, QuantifiedSample[T]](libraryAccess) with LazyLogging {

  @Autowired(required = false)
  val postprocessingInstructions: java.util.List[PostProcessing[T]] = new util.ArrayList[PostProcessing[T]]()

  /**
    * builds a sample containing the quantified data
    *
    * @param input
    * @return
    */
  final override def process(input: AnnotatedSample, targets: Iterable[Target]): QuantifiedSample[T] = {

    logger.debug(s"quantify sample: ${input.fileName}")
    /**
      * merge the found and none found targets, which can be later replaced or so
      */
    val resultList: Seq[QuantifiedTarget[T]] = targets.collect {
      case myTarget: Target =>
        val result = input.spectra.filter(_.target == myTarget)

        //nothing found for this spectra, needs to be replaced later
        if (result.isEmpty) {
          logger.debug(s"\t=> annotation not found for ${myTarget}")
          new QuantifiedTarget[T] {
            /**
              * value for this target
              */
            override val quantifiedValue: Option[T] = None
            /**
              * associated spectra
              */
            override val spectra: Option[_ <: Feature with QuantifiedSpectra[T]] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = myTarget.name
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = myTarget.retentionIndex
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = myTarget.inchiKey
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = myTarget.precursorMass
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = myTarget.confirmed
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = myTarget.requiredForCorrection
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = myTarget.isRetentionIndexStandard
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = myTarget.spectrum
          }
        }
        //associated with a target and quantified
        else {
          logger.debug(s"\t=> annotation found for ${myTarget}")
          new QuantifiedTarget[T] {
            /**
              * value for this target
              */
            override val quantifiedValue: Option[T] = computeValue(myTarget, result.head)
            /**
              * associated spectra
              */
            lazy override val spectra: Option[_ <: Feature with QuantifiedSpectra[T]] = Option(SpectraHelper.addQuantification(this, result.head))
            /**
              * a name for this spectra
              */
            override var name: Option[String] = myTarget.name
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = myTarget.retentionIndex
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = myTarget.inchiKey
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = myTarget.precursorMass
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = myTarget.confirmed
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = myTarget.requiredForCorrection
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = myTarget.isRetentionIndexStandard
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = myTarget.spectrum
          }
        }
    }.seq.toSeq.sortBy(_.retentionTimeInSeconds)

    buildResult(input, resultList)
  }

  /**
    * assemble our result data set
    *
    * @param input
    * @param resultList
    * @return
    */
  protected def buildResult(input: AnnotatedSample, resultList: Seq[QuantifiedTarget[T]]): QuantifiedSample[T] = {

    new QuantifiedSample[T] {
      override val quantifiedTargets: Seq[QuantifiedTarget[T]] = resultList
      override val fileName: String = input.fileName
      override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = input.noneAnnotated
      override val correctedWith: Sample = input.correctedWith
      override val featuresUsedForCorrection: Seq[TargetAnnotation[Target, Feature]] = input.featuresUsedForCorrection
      override val regressionCurve: Regression = input.regressionCurve
    }

  }

  /**
    * computes the actual value of the spectra or returns None if it wasn't possible
    *
    * @param target
    * @param spectra
    * @return
    */
  protected def computeValue(target: Target, spectra: Feature): Option[T]
}

/**
  * quantifies the data in the given sample by height
  *
  * @param properties
  */
@Component
@Profile(Array("carrot.report.quantify.height"))
class QuantifyByHeightProcess @Autowired()(libraryAccess: LibraryAccess[Target]) extends QuantificationProcess[Double](libraryAccess) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Double] = if (spectra.massOfDetectedFeature.isDefined) Some(spectra.massOfDetectedFeature.get.intensity) else None
}

/**
  * reports the scan for each annotated spectra, which can be used to confirm and fine tune the system
  *
  * @param properties
  */
@Component
@Profile(Array("quantify-by-scan"))
class QuantifyByScanProcess @Autowired()(libraryAccess: LibraryAccess[Target]) extends QuantificationProcess[Int](libraryAccess) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Int] = Some(spectra.scanNumber)
}
