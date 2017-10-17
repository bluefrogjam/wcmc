package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._

/**
  * Defines a target for a targeted based approach
  */
trait Target extends CorrectedSpectra with SimilaritySupport with AccurateMassSupport {
  /**
    * a name for this spectra
    */
  val name: Option[String]

  /**
    * the specified ionmode for this target. By default we should always assume that it's positive
    */
  val ionMode: IonMode = PositiveMode()

  /**
    * the retention index of this spectra
    */
  def retentionTimeInMinutes: Double = retentionIndex / 60

  /**
    * retention time in seconds of this target
    */
  val retentionIndex: Double

  /**
    * by default we report the retention time the same as the retention index
    * unless overwritten
    */
  val retentionTimeInSeconds: Double = retentionIndex
  /**
    * the unique inchi key for this spectra
    */
  val inchiKey: Option[String]

  /**
    * the mono isotopic mass of this spectra
    */
  val precursorMass: Option[Double]

  /**
    * is this a confirmed target
    */
  val confirmed: Boolean

  /**
    * is this target required for a successful retention index correction
    */
  val requiredForCorrection: Boolean

  /**
    * is this a retention index correction standard
    */
  val isRetentionIndexStandard: Boolean

  override def toString = f"Target(name=${name.getOrElse("None")}, retentionTime=$retentionTimeInMinutes (min), retentionTime=$retentionIndex (s), inchiKey=${inchiKey.getOrElse("None")}, monoIsotopicMass=${precursorMass.getOrElse("None")})"

  /**
  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case x: Target =>
        x.retentionIndex == retentionIndex && x.inchiKey.equals(inchiKey) && x.name.equals(name) && x.precursorMass.equals(precursorMass)
      case _ => false
    }
  }

    */

  /**
    * associated accurate mass
    *
    * @return
    */
  override def accurateMass: Option[Double] = precursorMass


  override def equals(that: Any): Boolean =
    that match {
      case that: Target =>
        (that.name, this.name) match {
          case (Some(thatName), Some(thisName)) if thatName == thisName =>
            (that.precursorMass, this.precursorMass) match {
              case (Some(thatMass), Some(thisMass)) if thatMass == thatMass =>
                that.retentionIndex == this.retentionIndex
              case _ => false
            }
          case _ => false
        }
      case _ => false
    }
}

/**
  * this defines an annotation for a target
  */
case class TargetAnnotation[T <: Target, A <: Feature](target: T, annotation: A)
