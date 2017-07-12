package edu.ucdavis.fiehnlab.ms.carrot.core.api

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}

/**
  * provides a couple of different methods, which should be used to create new instances of
  * spectra objects and keeping track of the correct implementations
  * Created by wohlgemuth on 7/12/17.
  */
object SpectraHelper {

  /**
    * TODO take care of default values, these are missing right now
    *
    * converts the feature into a feature with retention index correction
    *
    * @param feature
    * @param _retentionIndex
    * @return
    */
  def addCorrection(feature: Feature, _retentionIndex: Double): Feature with CorrectedSpectra = {

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
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

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

          override val retentionIndex: Double = _retentionIndex
        }
      case feat: MSSpectra =>
        new MSSpectra with CorrectedSpectra {
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

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

          override val retentionIndex: Double = _retentionIndex
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

          override val retentionIndex: Double = _retentionIndex
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

        }

    }
  }

  /**
    * defines the following feature as an annotated feauture.
    * @param feature
    * @param _massErrorPPM
    * @param _massError
    * @param _target
    * @return
    */
  def addAnnotation(feature: Feature with CorrectedSpectra, _massErrorPPM: Option[Double], _massError: Option[Double], _target: Target): Feature with AnnotatedSpectra = {

    feature match {
      case feat: MSMSSpectra with CorrectedSpectra =>
        new MSMSSpectra with AnnotatedSpectra {
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

          override val retentionIndex: Double = feat.retentionIndex
          /**
            * associated target
            */
          override val target: Target = target
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = _massError
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = _massErrorPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = Some(target.retentionTimeInSeconds - retentionIndex)

          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

        }
      case feat: MSSpectra with CorrectedSpectra =>
        new MSSpectra with AnnotatedSpectra {
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

          override val retentionIndex: Double = feat.retentionIndex
          /**
            * associated target
            */
          override val target: Target = target
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = _massError
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = _massErrorPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = Some(target.retentionTimeInSeconds - retentionIndex)
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

        }
      case feat: Feature with CorrectedSpectra =>

        new Feature with AnnotatedSpectra {
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber

          override val retentionIndex: Double = feat.retentionIndex
          /**
            * associated target
            */
          override val target: Target = target
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = _massError
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = _massErrorPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = Some(target.retentionTimeInSeconds - retentionIndex)
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
        }

    }
  }

  def addQuantification[T]():Feature with QuantifiedSpectra[T] = ???
}
