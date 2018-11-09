package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.springframework.beans.factory.annotation.Autowired

/**
  * defines a correction process
  *
  * @param libraryAccess
  */
abstract class CorrectionProcess @Autowired()(val libraryAccess: MergeLibraryAccess, val stasisClient: StasisService) extends AnnotationProcess[Sample, CorrectedSample](libraryAccess, stasisClient) with LazyLogging {

  val regression: Regression

  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  override final def process(input: Sample, target: Iterable[Target], method: AcquisitionMethod): CorrectedSample = {

    val retentionIndexMarkers = target.filter(_.isRetentionIndexStandard)
    var requiredTargets = retentionIndexMarkers.filter(_.requiredForCorrection)

    if (retentionIndexMarkers.isEmpty) {
      throw new ProcessException(s"Method ${AcquisitionMethod.serialize(method)} doesn't have retention index targets defined! please add some.")
    }

    val optimizedMatches = findCorrectionTargets(input, retentionIndexMarkers, method)


    //verify that we have all our tartes

    val missingButRequired = requiredTargets.collect {

      case target:Target if !optimizedMatches.exists(_.target == target) =>
        target
    }

    if(missingButRequired.nonEmpty){
      logger.warn("Missing annotations for:")

      missingButRequired.foreach{ x =>
        logger.warn(s"\t${x}")
      }
      throw new RequiredStandardNotFoundException("we were missing certain targets during the correction and so it failed",missingButRequired)
    }

    //do the actual correction and return the sample for further processingse
    doCorrection(optimizedMatches, input, regression, input)
  }


  /**
    * verifies that one standard is not annotate twice
    *
    * @param optimizedMatches
    * @return
    */
  def verifyAnnotations(optimizedMatches: Iterable[TargetAnnotation[Target, Feature]], input: Sample): Unit = {
    if (optimizedMatches.map(_.target).toSet.size != optimizedMatches.size) {
      throw new StandardAnnotatedTwice(s"one of the standards, was annotated twice in sample ${input.fileName}!")
    }

  }

  /**
    * minimum required count of standards found
    *
    * @return
    */
  protected def getMinimumFoundStandards: Int

  /**
    * check that the found annotations are enough to satisfy the need for correction
    *
    * @param possibleHits
    */
  def verifyCount(possibleHits: Iterable[TargetAnnotation[Target, Feature]], input: Sample): Unit = {
    //ensure we found enough standards
    if (possibleHits.size < getMinimumFoundStandards) {
      throw new NotEnoughStandardsFoundException(s"sorry we did not find enough standards in this sample: ${input.fileName} for a successful correction. We only found ${possibleHits.size}, but require ${getMinimumFoundStandards}")
    }
  }

  /**
    * verifies the order of the annotated retention index standards or throws an exception
    *
    * @param possibleHits
    */
  def verifyOrder(possibleHits: Iterable[TargetAnnotation[Target, Feature]], input: Sample): Unit = {
    possibleHits.foreach { x =>
      logger.info(f"validating order for ${x.target.name.get.substring(0, 15)} with ${x.target.retentionIndex}%.2f " +
          f"against annotation ${x.annotation.retentionTimeInSeconds}%.2f " +
          f"and intensity ${x.annotation.massOfDetectedFeature.getOrElse(Ion(0,0)).intensity}%.0f")
    }
    // brian would suggest to delete standards, which are out of order in case they are the same compound with different ionisations and come very close together
    // brian suggests to add a small 2s window in which the order of standards doesn't matter

    if (!possibleHits.sliding(2).forall(x => {
      if(x.head.target.inchiKey != x.last.target.inchiKey) {  // this prevents adducts from failing the check
        x.head.annotation.retentionTimeInSeconds <= x.last.annotation.retentionTimeInSeconds
      } else {
        true
      }
    })) {
      throw new StandardsNotInOrderException(s"one or more standards in sample ${input.fileName} where not annotated in ascending order of their retention times!")
    }
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
  protected def findCorrectionTargets(input: Sample, target: Iterable[Target], method: AcquisitionMethod): Iterable[TargetAnnotation[Target, Feature]]

  /**
    * executes the actual correction of the samples
    *
    * @param possibleHits
    * @param sampleToCorrect
    * @param regression
    * @return
    */
  def doCorrection(possibleHits: Iterable[TargetAnnotation[Target, Feature]], sampleToCorrect: Sample, regression: Regression, sampleUsedForCorrection: Sample, tracking: Boolean = true): CorrectedSample = {

    //make sure they are in numerical order
    verifyOrder(possibleHits, sampleUsedForCorrection)

    //verify we got enough standards
    verifyCount(possibleHits, sampleUsedForCorrection)

    //verify that there
    verifyAnnotations(possibleHits, sampleUsedForCorrection)

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

    val correctedSample = new CorrectedSample {
      //needs to be possible to replace this with a different sample later
      override val correctedWith: Sample = sampleUsedForCorrection

      //generate our new spectra collection
      override val spectra: Seq[_ <: Feature with CorrectedSpectra] = correctedSpectra

      //the original data, this sample is based on
      override val featuresUsedForCorrection: Iterable[TargetAnnotation[Target, Feature]] = possibleHits
      override val regressionCurve: Regression = regression
      override val fileName: String = sampleToCorrect.fileName

      override val properties: Option[SampleProperties] = sampleToCorrect.properties
    }

    // update stasis tracking data
    if (tracking)
      stasisClient.addTracking(TrackingData(correctedSample.name, "corrected", correctedSample.fileName))

    correctedSample
  }

}
