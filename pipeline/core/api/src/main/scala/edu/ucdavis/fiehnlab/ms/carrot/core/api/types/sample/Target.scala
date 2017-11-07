package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._

/**
  * Defines a target for a targeted based approach
  */
trait Target extends CorrectedSpectra with SimilaritySupport with AccurateMassSupport {

  /**
    * a name for this spectra
    */
  var name: Option[String]

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
  var inchiKey: Option[String]

  /**
    * the mono isotopic mass of this spectra
    */
  val precursorMass: Option[Double]

  /**
    * is this a confirmed target
    */
  var confirmed: Boolean

  /**
    * is this target required for a successful retention index correction
    */
  var requiredForCorrection: Boolean

  /**
    * is this a retention index correction standard
    */
  var isRetentionIndexStandard: Boolean

  override def toString = f"Target(name=${name.getOrElse("None")}, retentionTime=$retentionTimeInMinutes (min), retentionTime=$retentionIndex (s), inchiKey=${inchiKey.getOrElse("None")}, monoIsotopicMass=${precursorMass.getOrElse("None")})"

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case x: Target =>

        //ri needs to be identical
        if (x.retentionIndex.equals(retentionIndex)) {

          //pre cursor needs to be identical
          if (x.precursorMass.equals(precursorMass)) {

            //if inchi is defined on both sides, it needs to be identical
            val inchiIdentical = if (x.inchiKey.isDefined && inchiKey.isDefined) {
              x.inchiKey.equals(inchiKey)
            }
            else {
              true
            }

            if (inchiIdentical) {

              //if name is defined on both sides it needs to be identical
              if (x.name.isDefined && name.isDefined) {
                x.name.equals(name)
              }
              else {
                true
              }
            }
            else {
              false
            }
          }
          else {
            false
          }
        }
        else {
          false
        }
      case _ => false
    }
  }

  /**
    * associated accurate mass
    *
    * @return
    */
  override def accurateMass: Option[Double] = precursorMass

}

/**
  * this defines an annotation for a target
  */
case class TargetAnnotation[T <: Target, A <: Feature](target: T, annotation: A)
