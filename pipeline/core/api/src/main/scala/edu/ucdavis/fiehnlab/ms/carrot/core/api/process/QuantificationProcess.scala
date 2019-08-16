package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSpectra, Sample, Target, _}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * quantifies a sample, so it's ready to be exported
  */

abstract class QuantificationProcess[T](libraryAccess: MergeLibraryAccess, stasisClient: StasisService) extends AnnotationProcess[AnnotatedSample, QuantifiedSample[T]](libraryAccess, stasisClient) with Logging {

  @Autowired(required = false)
  val postprocessingInstructions: java.util.List[PostProcessing[T]] = new util.ArrayList[PostProcessing[T]]()

  /**
    * builds a sample containing the quantified data
    *
    * @param input
    * @return
    */
  final override def process(input: AnnotatedSample, targets: Iterable[Target], method: AcquisitionMethod, rawSample: Option[Sample]): QuantifiedSample[T] = {

    logger.info(s"quantifying sample: ${input.fileName}")
    /**
      * merge the found and not found targets, which can be later replaced or so
      */
    val resultList: Seq[QuantifiedTarget[T]] = targets.collect {
      case myTarget: Target =>
        val result = input.spectra.filter(_.target == myTarget)

        //nothing found for this spectra, needs to be replaced later
        if (result.isEmpty) {
          logger.debug(s"\t=> annotation not found for ${myTarget}")
          new QuantifiedTarget[T] {
            override val uniqueMass: Option[Double] = myTarget.uniqueMass
            override val quantifiedValue: Option[T] = None
            override val spectra: Option[_ <: Feature with QuantifiedSpectra[T]] = None
            override var name: Option[String] = myTarget.name
            override val retentionIndex: Double = input.regressionCurve.computeY(myTarget.retentionTimeInSeconds) // myTarget.retentionTimeInSeconds
            override val retentionTimeInSeconds: Double = myTarget.retentionTimeInSeconds
            override var inchiKey: Option[String] = myTarget.inchiKey
            override val precursorMass: Option[Double] = myTarget.precursorMass
            override var confirmed: Boolean = myTarget.confirmed
            override var requiredForCorrection: Boolean = myTarget.requiredForCorrection
            override var isRetentionIndexStandard: Boolean = myTarget.isRetentionIndexStandard
            override val spectrum: Option[SpectrumProperties] = myTarget.spectrum
            override val ionMode: IonMode = myTarget.ionMode
          }
        }
        //associated with a target and quantified
        else {
          logger.debug(s"\t=> annotation found for ${myTarget.name.get}")
          new QuantifiedTarget[T] {
            override val uniqueMass: Option[Double] = myTarget.uniqueMass
            override val quantifiedValue: Option[T] = computeValue(myTarget, result.head)
            lazy override val spectra: Option[_ <: Feature with QuantifiedSpectra[T]] = Option(SpectraHelper.addQuantification(this, result.head))
            override var name: Option[String] = myTarget.name
            override val retentionIndex: Double = result.head.retentionIndex
            override val retentionTimeInSeconds: Double = myTarget.retentionTimeInSeconds
            override var inchiKey: Option[String] = result.head.target.inchiKey
            override val precursorMass: Option[Double] = result.head.accurateMass
            override var confirmed: Boolean = myTarget.confirmed
            override var requiredForCorrection: Boolean = myTarget.requiredForCorrection
            override var isRetentionIndexStandard: Boolean = myTarget.isRetentionIndexStandard
            override val spectrum: Option[SpectrumProperties] = result.head.associatedScan
            override val ionMode: IonMode = result.head.ionMode.get
          }
        }
    }.seq.toSeq.sortBy(t => (t.name.get, -t.retentionTimeInSeconds))

    stasisClient.addTracking(TrackingData(input.name, "quantified", input.fileName))

    var result = buildResult(input, resultList)

    // apply post processing
    postprocessingInstructions.asScala.foreach { x =>
      logger.info(s"executing: ${x.getClass.getSimpleName}")
      result = x.process(result, method, rawSample)
    }

    result
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
      override val featuresUsedForCorrection: Iterable[TargetAnnotation[Target, Feature]] = input.featuresUsedForCorrection
      override val regressionCurve: Regression = input.regressionCurve
      override val properties: Option[SampleProperties] = input.properties
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




