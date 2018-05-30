package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties

case class LCMSCorrectionTarget(target: LCMSRetentionIndexTargetConfiguration) extends Target {

  val config: LCMSRetentionIndexTargetConfiguration = target
  /**
    * a name for this spectra
    */
  override var name: Option[String] = Option(target.identifier)
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = target.retentionIndex
  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = None
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = None
  /**
    * is this a confirmed target
    */
  override var confirmed: Boolean = true
  /**
    * is this target required for a successful retention index correction
    */
  override var requiredForCorrection: Boolean = false
  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = true
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = None
  /**
    * unique mass for a given target
    */
  override val uniqueMass: Option[Double] = None
}
