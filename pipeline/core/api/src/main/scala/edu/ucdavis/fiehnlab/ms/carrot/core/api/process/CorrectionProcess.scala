package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Sample, Target, TargetAnnotation}
import org.springframework.beans.factory.annotation.Autowired

/**
  * defines a correction process
  * @param libraryAccess
  */
abstract class CorrectionProcess @Autowired()(val libraryAccess: LibraryAccess[Target]) extends AnnotationProcess[Target, Sample, CorrectedSample](libraryAccess) with LazyLogging{

  val regression: Regression

  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  override final def process(input: Sample, target: Iterable[Target], method: AcquisitionMethod): CorrectedSample = {

    val optimizedMatches: Seq[TargetAnnotation[Target, Feature]] = findCorrectionTargets(input, target,method)

    //do the actual correction and return the sample for further processing
    doCorrection(optimizedMatches, input, regression, input)
  }

  /**
    * abstract method to acutall find out targets
    * required for the correction
    * of this sample
    *
    * @param input
    * @param target
    * @return
    */
  protected def findCorrectionTargets(input: Sample, target: Iterable[Target],method: AcquisitionMethod): Seq[TargetAnnotation[Target, Feature]]

    /**
    * executes the actual correction of the samples
    *
    * @param possibleHits
    * @param sampleToCorrect
    * @param regression
    * @return
    */
  def doCorrection(possibleHits: Seq[TargetAnnotation[Target, Feature]], sampleToCorrect: Sample, regression: Regression, sampleUsedForCorrection: Sample): CorrectedSample = {

    //library
    val y: Array[Double] = possibleHits.map(_.target.retentionIndex.toDouble).toArray

    //annotations
    val x: Array[Double] = possibleHits.map(_.annotation.retentionTimeInSeconds.toDouble).toArray

    regression.calibration(x, y)

    logger.info(s"${regression.toString}\n")

    val correctedSpectra: Seq[_ <: Feature with CorrectedSpectra] = sampleToCorrect.spectra.map(x => SpectraHelper.addCorrection(x, regression.computeY(x.retentionTimeInSeconds)))

    /**
      * generates a new corrected sample object
      * and computes all it's properties
      */

    new CorrectedSample {
      //needs to be possible to replace this with a different sample later
      override val correctedWith: Sample = sampleUsedForCorrection

      //generate our new spectra collection
      override val spectra: Seq[_ <: Feature with CorrectedSpectra] = correctedSpectra

      //the original data, this sample is based on
      override val featuresUsedForCorrection: Seq[TargetAnnotation[Target, Feature]] = possibleHits
      override val regressionCurve: Regression = regression
      override val fileName: String = sampleToCorrect.fileName
    }

  }

}
