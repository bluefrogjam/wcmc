package edu.ucdavis.fiehnlab.ms.carrot.core.api

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Ion}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}

/**
  * Created by wohlgemuth on 7/12/17.
  */
object SpectraHelper {

  /**
    * TODO take care of default values, these are missing right now
    *
    * converts the feature into a feature with retention index correction
    *
    * @param feature
    * @param retentionIndex
    * @return
    */
  def addCorrection(feature: Feature, retentionIndex: Double): Feature with CorrectedSpectra = {

    feature match {
      case feat: MSMSSpectra =>
        new MSMSSpectra with CorrectedSpectra {
          /**
            * the observed pre cursor ion
            */
          override val precursorIon: Double = feat.precursorIon
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * a list of model ions used during the deconvolution
            */
          override val modelIons: Option[Seq[Double]] = feat.modelIons
          /**
            * all the defined ions for this spectra
            */
          override val ions: Seq[Ion] = feat.ions
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds

          override val retentionIndex: Double = retentionIndex
        }
      case feat: MSSpectra =>
        new MSSpectra with CorrectedSpectra {
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * a list of model ions used during the deconvolution
            */
          override val modelIons: Option[Seq[Double]] = feat.modelIons
          /**
            * all the defined ions for this spectra
            */
          override val ions: Seq[Ion] = feat.ions
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds

          override val retentionIndex: Double = retentionIndex
        }
      case feat: Feature =>

        new Feature with CorrectedSpectra {
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber

          override val retentionIndex: Double = retentionIndex
        }

    }
  }
}
