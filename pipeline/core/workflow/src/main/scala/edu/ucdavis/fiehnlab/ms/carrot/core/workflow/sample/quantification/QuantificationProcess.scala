package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Regression}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSpectra, RetentionIndexTarget, Sample, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * quantifies a sample, so it's ready to be exported
  */

abstract class QuantificationProcess[T](libraryAccess: LibraryAccess[Target], properties: WorkflowProperties) extends AnnotationProcess[Target, AnnotatedSample, QuantifiedSample[T]](libraryAccess, properties.trackChanges) with LazyLogging {
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
            override val name: Option[String] = myTarget.name
            /**
              * retention time in seconds of this target
              */
            override val retentionTimeInSeconds: Double = myTarget.retentionTimeInSeconds
            /**
              * the unique inchi key for this spectra
              */
            override val inchiKey: Option[String] = myTarget.inchiKey
            /**
              * the mono isotopic mass of this spectra
              */
            override val monoIsotopicMass: Option[Double] = myTarget.monoIsotopicMass
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
            lazy override val spectra: Option[_ <: Feature with QuantifiedSpectra[T]] = Option(SpectraHelper.addQuantification(this,result.head))
            /**
              * a name for this spectra
              */
            override val name: Option[String] = myTarget.name
            /**
              * retention time in seconds of this target
              */
            override val retentionTimeInSeconds: Double = myTarget.retentionTimeInSeconds
            /**
              * the unique inchi key for this spectra
              */
            override val inchiKey: Option[String] = myTarget.inchiKey
            /**
              * the mono isotopic mass of this spectra
              */
            override val monoIsotopicMass: Option[Double] = myTarget.monoIsotopicMass
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
      override val featuresUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, Feature]] = input.featuresUsedForCorrection
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
class QuantifyByHeightProcess @Autowired()(libraryAccess: LibraryAccess[Target], properties: WorkflowProperties, postprocessingInstructions: List[PostProcessing[Double]]) extends QuantificationProcess[Double](libraryAccess, properties) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Double] = MassAccuracy.findClosestIon(spectra, target.monoIsotopicMass.get /*, 5*/) match {
    case Some(x: Ion) => Some(x.intensity)
    case None => None
  }
}

/**
  * reports the scan for each annotated spectra, which can be used to confirm and fine tune the system
  *
  * @param properties
  */
@Component
class QuantifyByScanProcess @Autowired()(libraryAccess: LibraryAccess[Target], properties: WorkflowProperties, postprocessingInstructions: List[PostProcessing[Int]]) extends QuantificationProcess[Int](libraryAccess, properties) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Int] = Some(spectra.scanNumber)
}
