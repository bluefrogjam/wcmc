package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._

/**
  * Defines a target for a targeted based approach
  */
trait Target extends CorrectedSpectra with SimilaritySupport with AccurateMassSupport with Serializable with Indexed {

  val idx: Int

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
  def retentionTimeInSeconds: Double = retentionIndex
  /**
    * the unique inchi key for this spectra
    */
  var inchiKey: Option[String]

  /**
    * the mono isotopic mass of this spectra
    */
  val precursorMass: Option[Double]

  /**
    * unique mass for a given target
    */
  val uniqueMass: Option[Double]

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

  override def toString: String = f"Target(idx=$idx, name=${name.getOrElse("None")}, " +
      f"retentionTimeMinutes=$retentionTimeInMinutes, " +
      f"retentionIndex=$retentionIndex, accurateMass=${accurateMass.getOrElse("NA")}, " +
      f"inchiKey=${inchiKey.getOrElse("None")}, monoIsotopicMass=${precursorMass.getOrElse("None")}, " +
      f"${if (isRetentionIndexStandard) "ISTD" else ""})"

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

trait Indexed {
  val idx: Int = 0
}

trait CorrectionTarget extends Target

trait AnnotationTarget extends Target

/**
  * this defines an annotation for a target
  */
case class TargetAnnotation[T <: Target, A <: Feature](target: T, annotation: A) {
  override def toString: String = {
    f"${target.name.getOrElse("Unknown")} (${target.precursorMass.getOrElse(-1.0)}%.4f @ ${target.retentionIndex}%.2f <==> " +
        f"(${annotation.massOfDetectedFeature.getOrElse(Ion(-1,0)).mass}%.4f @ ${annotation.retentionTimeInSeconds}%.2f) : " +
        f"${annotation.massOfDetectedFeature.getOrElse(Ion(-1,0)).intensity}%.0f"
  }
}
