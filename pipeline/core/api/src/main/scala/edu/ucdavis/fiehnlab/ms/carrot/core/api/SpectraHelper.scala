package edu.ucdavis.fiehnlab.ms.carrot.core.api

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._

/**
  * provides a couple of different methods, which should be used to create new instances of
  * spectra objects and keeping track of the correct implementations
  * Created by wohlgemuth on 7/12/17.
  */
object SpectraHelper {


  def addMassCalibration(feature: Feature, spectrumProperties: SpectrumProperties, correctedMassOfDetectedFeature: Ion): Feature = {

    feature match {
      case feat: MSMSSpectra =>

        new MSMSSpectra {

          override val sample: String = feature.sample
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
          override val massOfDetectedFeature: Option[Ion] = Some(correctedMassOfDetectedFeature)

          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan

          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds

          override val precursorScan: Option[SpectrumProperties] = feat.precursorScan
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feature.signalNoise
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feature.uniqueMass
          /**
            * Contains random metadata associated to the object we mix this into
            */
          override val metadata: Map[String, AnyRef] = feat.metadata
        }
      case feat: MSSpectra =>
        new MSSpectra {
          override val sample: String = feature.sample
          /**
            * how pure this spectra is
            */
          override val metadata: Map[String, AnyRef] = feat.metadata
          override val purity: Option[Double] = feat.purity
          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = Some(correctedMassOfDetectedFeature)

          override val associatedScan: Option[SpectrumProperties] = Some(spectrumProperties)
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feature.signalNoise
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feature.uniqueMass
        }
      case feat: Feature =>

        new Feature {
          override val sample: String = feature.sample
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          /**
            * the local scan number
            */
          override val scanNumber: Int = feat.scanNumber

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
          override val massOfDetectedFeature: Option[Ion] = Some(correctedMassOfDetectedFeature)

          override val associatedScan: Option[SpectrumProperties] = Some(spectrumProperties)
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feature.signalNoise
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feature.uniqueMass
          override val metadata: Map[String, AnyRef] = feat.metadata
        }

    }
  }

  /**
    * TODO take care of default values, these are missing right now
    *
    * converts the feature into a feature with retention index correction
    *
    * @param feature         non corrected feature
    * @param _retentionIndex corrected retention time (after calibration)
    * @return
    */
  def addCorrection(feature: Feature, _retentionIndex: Double): Feature with CorrectedSpectra = {

    feature match {
      case feat: MSMSSpectra =>

        new MSMSSpectra with CorrectedSpectra {
          override val sample: String = feat.sample
          override val precursorIon: Double = feat.precursorIon
          override val purity: Option[Double] = feat.purity
          override val ionMode: Option[IonMode] = feat.ionMode

          // shouldn't this be the Ion(feat.accmass, feat.intensity @ corrected RT)?
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature

          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
          override val scanNumber: Int = feat.scanNumber

          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          override val retentionIndex: Double = _retentionIndex

          override val precursorScan: Option[SpectrumProperties] = feat.precursorScan
          override val signalNoise: Option[Double] = feat.signalNoise
          override val uniqueMass: Option[Double] = feat.uniqueMass
          override val metadata: Map[String, AnyRef] = feat.metadata
        }
      case feat: MSSpectra =>
        new MSSpectra with CorrectedSpectra {
          override val sample: String = feat.sample
          override val purity: Option[Double] = feat.purity
          override val ionMode: Option[IonMode] = feat.ionMode
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
          override val scanNumber: Int = feat.scanNumber

          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          override val retentionIndex: Double = _retentionIndex

          override val signalNoise: Option[Double] = feat.signalNoise
          override val uniqueMass: Option[Double] = feat.uniqueMass
          override val metadata: Map[String, AnyRef] = feat.metadata
        }
      case feat: Feature =>

        new Feature with CorrectedSpectra {
          override val sample: String = feat.sample
          override val scanNumber: Int = feat.scanNumber

          override val retentionTimeInSeconds: Double = feat.retentionTimeInSeconds
          override val retentionIndex: Double = _retentionIndex

          override val purity: Option[Double] = feat.purity
          override val ionMode: Option[IonMode] = feat.ionMode
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
          override val signalNoise: Option[Double] = feat.signalNoise
          override val uniqueMass: Option[Double] = feat.uniqueMass
          override val metadata: Map[String, AnyRef] = feat.metadata
        }

    }
  }

  /**
    * defines the following feature as an annotated feature.
    *
    * @param feature
    * @param _massErrorPPM
    * @param _massError
    * @param _target library target used in the correction
    * @return
    */
  def addAnnotation(feature: Feature with CorrectedSpectra, _massErrorPPM: Option[Double], _massError: Option[Double], _target: Target): Feature with AnnotatedSpectra = {

    feature match {
      case feat: MSMSSpectra with CorrectedSpectra =>
        new MSMSSpectra with AnnotatedSpectra {
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feat.signalNoise
          override val sample: String = feature.sample
          /**
            * the observed pre cursor ion
            */
          override val precursorIon: Double = feat.precursorIon

          override val precursorScan: Option[SpectrumProperties] = feat.precursorScan

          override val metadata: Map[String, AnyRef] = feat.metadata
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          /**
            * a list of model ions used during the deconvolution
            */
          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan

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
          override val target: Target = _target
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
          lazy override val retentionIndexDistance: Option[Double] = Some(_target.retentionIndex - retentionIndex)

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
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feat.signalNoise
          override val sample: String = feature.sample

          override val metadata: Map[String, AnyRef] = feat.metadata
          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
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
          override val target: Target = _target
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
          lazy override val retentionIndexDistance: Option[Double] = Some(_target.retentionIndex - retentionIndex)
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
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feat.signalNoise
          override val metadata: Map[String, AnyRef] = feat.metadata
          override val sample: String = feature.sample
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
          override val target: Target = _target
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
          lazy override val retentionIndexDistance: Option[Double] = Some(_target.retentionIndex - retentionIndex)
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

          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
        }

    }
  }

  def addQuantification[T](quantified: QuantifiedTarget[T], feature: Feature with AnnotatedSpectra): Feature with QuantifiedSpectra[T] = {

    feature match {
      case feat: MSMSSpectra with AnnotatedSpectra =>
        new MSMSSpectra with QuantifiedSpectra[T] {
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feat.signalNoise
          override val sample: String = feat.sample
          /**
            * the observed pre cursor ion
            */
          override val precursorIon: Double = feat.precursorIon

          override val precursorScan: Option[SpectrumProperties] = feat.precursorScan

          override val metadata: Map[String, AnyRef] = feat.metadata
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan

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
          override val target: Target = quantified
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = feat.massAccuracy
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = feat.massAccuracyPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = feat.retentionIndexDistance

          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
          /**
            * value for this target
            */
          override val quantifiedValue: Option[T] = quantified.quantifiedValue
        }
      case feat: MSSpectra with AnnotatedSpectra =>
        new MSSpectra with QuantifiedSpectra[T] {
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feat.signalNoise
          override val sample: String = feature.sample
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          override val metadata: Map[String, AnyRef] = feat.metadata

          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
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
          override val target: Target = quantified
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = feat.massAccuracy
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = feat.massAccuracyPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = feat.retentionIndexDistance

          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
          /**
            * value for this target
            */
          override val quantifiedValue: Option[T] = quantified.quantifiedValue

        }
      case feat: Feature with AnnotatedSpectra =>

        new Feature with QuantifiedSpectra[T] {
          /**
            * the unique mass of this spectra
            */
          override val uniqueMass: Option[Double] = feat.uniqueMass
          /**
            * the signal noise of this spectra
            */
          override val signalNoise: Option[Double] = feature.signalNoise
          override val sample: String = feature.sample
          /**
            * how pure this spectra is
            */
          override val purity: Option[Double] = feat.purity
          override val metadata: Map[String, AnyRef] = feat.metadata

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
          override val target: Target = quantified
          /**
            * mass accuracy
            */
          override val massAccuracy: Option[Double] = feat.massAccuracy
          /**
            * accyracy in ppm
            */
          override val massAccuracyPPM: Option[Double] = feat.massAccuracyPPM
          /**
            * distance of the retention index distance
            */
          lazy override val retentionIndexDistance: Option[Double] = feat.retentionIndexDistance

          /**
            * specified ion mode for the given feature
            */
          override val ionMode: Option[IonMode] = feat.ionMode
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature: Option[Ion] = feat.massOfDetectedFeature
          /**
            * value for this target
            */
          override val quantifiedValue: Option[T] = quantified.quantifiedValue

          override val associatedScan: Option[SpectrumProperties] = feat.associatedScan
        }

    }
  }
}
