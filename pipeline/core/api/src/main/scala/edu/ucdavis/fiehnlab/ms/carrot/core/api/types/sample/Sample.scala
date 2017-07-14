package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}

/**
  * Defines a basic sample, which needs to be processed
  */
trait Sample {
  /**
    * a collection of spectra
    * belonging to this sample
    */
  val spectra: Seq[_ <: Feature]

  /**
    * the unique file name of the sample
    */
  val fileName: String

  /**
    * internal name of the sample
    */
  lazy val name: String = if (fileName.contains(".")) fileName.substring(0, fileName.indexOf(".")) else fileName

  override def toString = s"Sample(${spectra.size} spectra and file name is $fileName), unique name is $name"
}

/**
  * this defines a sample, which has been processed by the system
  */
trait ProcessedSample extends Sample {
}

/**
  * a sample, which has been corrected
  *
  */
trait CorrectedSample extends ProcessedSample {
  /**
    * which sample was used to correct these data
    */
  val correctedWith: Sample

  /**
    * did the correction fail and a different sample got used for the correction
    */
  def correctionFailed: Boolean = correctedWith.fileName != this.fileName

  /**
    * the curve, which was utilized for this correction
    */
  val regressionCurve: Regression

  /**
    * these are all the targets, which were used for the retention index correction
    */
  val featuresUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, Feature]]

  /**
    * the associated spectra, which are now corrected
    */
  override val spectra: Seq[_ <: Feature with CorrectedSpectra]

}

/**
  * this defines a sample, which has annotated spectra
  */
trait AnnotatedSample extends CorrectedSample {

  /**
    * the associated spectra, which are now corrected
    */
  override val spectra: Seq[_ <: Feature with AnnotatedSpectra]

  /**
    * these spectra had no matching targets
    */
  val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra]
}

/**
  * provides a quantified sample, which has value information's, etc associated
  */
trait QuantifiedSample[T] extends AnnotatedSample with LazyLogging {

  /**
    * associated spectra, which have been quantified with the associated target
    */
  override lazy val spectra: Seq[_ <: Feature with QuantifiedSpectra[T]] = quantifiedTargets.filter(_.spectra.isDefined).map { x =>
    x.spectra.get
  }

  /**
    * quantified targets for this samples
    */
  val quantifiedTargets: Seq[QuantifiedTarget[T]]
}

/**
  * a corrected spectra
  */
trait CorrectedSpectra {
  val retentionIndex: Double
}

/**
  * an annotated spectra
  */
trait AnnotatedSpectra extends CorrectedSpectra {
  /**
    * associated target
    */
  val target: Target

  /**
    * mass accuracy
    */
  val massAccuracy: Option[Double]

  /**
    * accyracy in ppm
    */
  val massAccuracyPPM: Option[Double]

  /**
    * distance of the retention index distance
    */
  val retentionIndexDistance: Option[Double]

}

/**
  * a quantified value for a given target
  *
  * @tparam T
  */
trait QuantifiedSpectra[T] extends AnnotatedSpectra {

  /**
    * value for this target
    */
  val quantifiedValue: Option[T]

  override def toString = s"QuantifiedSpectra(quantifiedValue=$quantifiedValue, target=$target)"
}


/**
  * a quantified value for a given target
  *
  * @tparam T
  */
trait QuantifiedTarget[T] extends Target {

  /**
    * value for this target
    */
  val quantifiedValue: Option[T]

  /**
    * associated spectra
    */
  val spectra: Option[_ <: Feature with QuantifiedSpectra[T]]

  override def toString = s"QuantifiedTarget(quantifiedValue=$quantifiedValue, name=$name, rt=$retentionTimeInSeconds"
}

trait GapFilledTarget[T] extends QuantifiedTarget[T]{
  override def toString = s"GapFilledTarget(quantifiedValue=$quantifiedValue, name=$name, rt=$retentionTimeInSeconds"

}

class ProxySample(fName: String, loader: SampleLoader) extends Sample with LazyLogging {
  /**
    * load the spectra once they are needed, but not before, but don't execute this more than once
    */
  lazy override val spectra: Seq[_ <: Feature] = {
    logger.debug(s"loading spectra from ${fName}...")
    loader.getSample(fName).spectra
  }

  /**
    * the unique file name of the sample
    */
  override val fileName: String = fName

  override def toString = s"Sample($name)"
}
