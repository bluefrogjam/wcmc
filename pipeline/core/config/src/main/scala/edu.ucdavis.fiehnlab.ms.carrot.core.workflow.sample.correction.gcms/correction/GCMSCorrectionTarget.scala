package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}

case class GCMSCorrectionTarget(target: GCMSRetentionIndexTargetConfiguration) extends Target {

  val config: GCMSRetentionIndexTargetConfiguration = target
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
  override var requiredForCorrection: Boolean = target.required

  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = true
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = Option(new SpectrumProperties {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = target.spectra.split(" ").map { x =>

      val v = x.split(":")
      Ion(v(0).toDouble, v(1).toFloat)
    }
    /**
      * the msLevel of this spectra
      */
    override val msLevel: Short = 1
  })
  /**
    * unique mass for a given target
    */
  override val uniqueMass: Option[Double] = Option(target.uniqueMass)
}
