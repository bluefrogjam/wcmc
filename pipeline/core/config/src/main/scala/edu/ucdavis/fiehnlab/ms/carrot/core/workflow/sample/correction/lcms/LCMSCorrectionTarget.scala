package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectionTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties

case class LCMSCorrectionTarget(lcconfig: LCMSRetentionIndexTargetConfiguration) extends CorrectionTarget {

  val config: LCMSRetentionIndexTargetConfiguration = lcconfig

  override def accurateMass: Option[Double] = Some(lcconfig.accurateMass)

  /**
    * a name for this spectra
    */
  override var name: Option[String] = Some(lcconfig.identifier)
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = {
    lcconfig.retentionIndex match {
      case 0 => lcconfig.retentionTimeUnit match {
        case "minutes" => lcconfig.retentionTime * 60
        case "seconds" => lcconfig.retentionTime
        case _ => 0.0
      }
      case _ => lcconfig.retentionIndex
    }
  }
  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = None
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = Some(lcconfig.accurateMass)
  /**
    * is this a confirmed target
    */
  override var confirmed: Boolean = lcconfig.confirmed
  /**
    * is this target required for a successful retention index correction
    */
  override var requiredForCorrection: Boolean = lcconfig.requiredForCorrection
  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = lcconfig.isInternalStandard
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = None
  /**
    * unique mass for a given target
    */
  override val uniqueMass: Option[Double] = None
}
