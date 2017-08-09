package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}

/**
  * Defines a target for a targeted based approach
  */
trait Target {
  /**
    * a name for this spectra
    */
  val name: Option[String]

  /**
    * the retention index of this spectra
    */
  def retentionTimeInMinutes: Double = retentionTimeInSeconds / 60

  /**
    * retention time in seconds of this target
    */
  val retentionTimeInSeconds:Double

  /**
    * the unique inchi key for this spectra
    */
  val inchiKey: Option[String]

  /**
    * the mono isotopic mass of this spectra
    */
  val monoIsotopicMass: Option[Double]

  /**
    * is this a confirmed target
    */
  val confirmedTarget:Boolean

  /**
    * is this target required for a successful retention index correction
    */
  val requiredForCorrection:Boolean

  /**
    * is this a retention index correction standard
    */
  val isRetentionIndexStandard:Boolean

  override def toString = f"Target(name=${name.getOrElse("None")}, retentionTime=$retentionTimeInMinutes (min), retentionTime=$retentionTimeInSeconds (s), inchiKey=${inchiKey.getOrElse("None")}, monoIsotopicMass=${monoIsotopicMass.getOrElse("None")})"

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case x:Target =>
        x.retentionTimeInSeconds == retentionTimeInSeconds && x.inchiKey.equals(inchiKey) && x.name.equals(name) && x.monoIsotopicMass.equals(monoIsotopicMass)
      case _ => false
    }
  }

}


/**
  * this defines an annotation for a target
  */
case class TargetAnnotation[T <: Target, A <: Feature](target: T, annotation: A)
