package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import com.typesafe.scalalogging.LazyLogging
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
  val annotationsUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, MSSpectra]]

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
  override val spectra: Seq[_ <: Feature with AnnotatedSpectra with CorrectedSpectra]

  /**
    * these spectra had no matching targets
    */
  val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra]
}

/**
  * provides a quantified sample, which has value information's, etc associated
  */
trait QuantifiedSample[T] extends AnnotatedSample with LazyLogging{

  /**
    * associated spectra, which have been quantified with the associated target
    */
  override lazy val spectra: Seq[_ <: Feature with QuantifiedSpectra[T]] = quantifiedTargets.collect {
    case x: QuantifiedSpectra[T] =>
      x.spectra match {

        /**
          * msms spectra
          */
        case Some(msms: MSMSSpectra with AnnotatedSpectra with CorrectedSpectra) =>

          /**
            * creates a new instance of a MSMS spectra
            */
          new MSMSSpectra with QuantifiedSpectra[T] {
            override val precursorIon: Double = msms.precursorIon
            override val spectra: Option[_ <: Feature with CorrectedSpectra] = x.spectra
            override val target: Target = x.target
            override val quantifiedValue: Option[T] = x.quantifiedValue
            override val ionMode: Option[IonMode] = msms.ionMode
            override val purity: Option[Double] = msms.purity
            override val scanNumber: Int = msms.scanNumber
            override val ions: Seq[Ion] = msms.ions
            override val modelIons: Option[Seq[Double]] = msms.modelIons
            override val msLevel: Short = msms.msLevel
            override val retentionTimeInSeconds: Double = msms.retentionTimeInSeconds
            override val massAccuracy: Option[Double] = msms.massAccuracy
            override val retentionIndexDistance: Option[Double] = msms.retentionIndexDistance
            override val massAccuracyPPM: Option[Double] = msms.massAccuracyPPM
            override val retentionIndex: Double = msms.retentionIndex
          }

        /**
          * ms spectra
          */
        case Some(ms: MSSpectra with AnnotatedSpectra with CorrectedSpectra) =>
          new MSSpectra with QuantifiedSpectra[T] {
            override val ionMode: Option[IonMode] = ms.ionMode
            override val purity: Option[Double] = ms.purity
            override val scanNumber: Int = ms.scanNumber
            override val ions: Seq[Ion] = ms.ions
            override val modelIons: Option[Seq[Double]] = ms.modelIons
            override val msLevel: Short = ms.msLevel
            override val retentionTimeInSeconds: Double = ms.retentionTimeInSeconds
            override val spectra: Option[_ <: Feature with CorrectedSpectra] = x.spectra
            override val target: Target = x.target
            override val quantifiedValue: Option[T] = x.quantifiedValue
            override val massAccuracy: Option[Double] = ms.massAccuracy
            override val retentionIndexDistance: Option[Double] = ms.retentionIndexDistance
            override val massAccuracyPPM: Option[Double] = ms.massAccuracyPPM
            override val retentionIndex: Double = ms.retentionIndex
          }

        /**
          * anything else
          */
        case None =>  null
      }
  }.collect {
    case x: Feature with QuantifiedSpectra[T] => x
  }

  /**
    * a list of all our targets used during matching
    * and can contain empty annotations
    */
  val quantifiedTargets: Seq[QuantifiedSpectra[T]]
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

  /**
    * associated spectra
    */
  val spectra: Option[_ <: Feature with CorrectedSpectra]

  override def toString = s"QuantifiedSpectra(quantifiedValue=$quantifiedValue, target=$target, spectra=$spectra)"
}

/**
  * a spectra which has been gap replaced
  *
  * @tparam T
  */
trait GapFillerSpectra[T] extends QuantifiedSpectra[T]{
  override def toString = s"GapFillerSpectra(quantifiedValue=$quantifiedValue, target=$target, spectra=$spectra)"

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
