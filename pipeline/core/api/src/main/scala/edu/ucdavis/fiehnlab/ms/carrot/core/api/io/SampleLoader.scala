package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io.{File, FileNotFoundException}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample, SampleProperties}
import org.apache.logging.log4j.scala.Logging
import org.springframework.cache.annotation.Cacheable

/**
  * used to simply load samples from a local or remote storage depending on
  * implementation
  */
trait SampleLoader extends Logging {

  /**
    * loads a sample as an option, so that we can evaluate it we have it or not, without an exception
    *
    * this will bever be cached!
    *
    * @param name
    * @return
    */
  def loadSample(name: String): Option[_ <: Sample]

  /**
    * forcefully loads the sample or throws a FileNotFoundException if it was not found
    *
    * @param name
    * @return
    */
  @Cacheable(value = Array("resource-get-sample"), key = "#name")
  def getSample(name: String): Sample = {
    loadSample(name) match {
      case None =>
        throw new FileNotFoundException(s"sorry the specified sample '${name}' was not found!")
      case Some(x) =>

        logger.info("wrapping sample to make it serializable")

        assert(x.spectra != null, "sorry your loaded object must contain some spectra!")
        val wrapped_spectra = x.spectra.map {
          case x: MSMSSpectra =>

            val _precursorIon = x.precursorIon
            val _sample = x.sample
            val _purity = x.purity
            val _signalNoise = x.signalNoise
            val _uniqueMass = x.uniqueMass
            val _scanNumber = x.scanNumber
            val _retentionTimeInSeconds = x.retentionTimeInSeconds
            val _ionMode = x.ionMode
            val _massOfDetectedFeature = x.massOfDetectedFeature

            val _precursorScan = x.precursorScan match {
              case Some(p) =>
                Some(
                  new SpectrumProperties {
                    /**
                      * the msLevel of this spectra
                      */
                    override val msLevel: Short = p.msLevel
                    /**
                      * a list of model ions used during the deconvolution
                      */
                    override val modelIons: Option[Seq[Double]] = p.modelIons
                    /**
                      * all the defined ions for this spectra
                      */
                    override val ions: Seq[Ion] = p.ions
                  }
                )

              case None => None
            }


            val _associatedScan = x.associatedScan match {
              case Some(p) =>
                Some(
                  new SpectrumProperties {
                    /**
                      * the msLevel of this spectra
                      */
                    override val msLevel: Short = p.msLevel
                    /**
                      * a list of model ions used during the deconvolution
                      */
                    override val modelIons: Option[Seq[Double]] = p.modelIons
                    /**
                      * all the defined ions for this spectra
                      */
                    override val ions: Seq[Ion] = p.ions
                  }
                )

              case None => None
            }


            new MSMSSpectra {
              /**
                * the observed pre cursor ion
                */
              override val precursorIon: Double = _precursorIon
              /**
                * the precursor scan info for the current MSMS spectrum
                */
              override val precursorScan: Option[SpectrumProperties] = _precursorScan
              /**
                * the associated sample
                */
              override val sample: String = _sample
              /**
                * how pure this spectra is
                */
              override val purity: Option[Double] = _purity
              /**
                * the signal noise of this spectra
                */
              override val signalNoise: Option[Double] = _signalNoise
              /**
                * the unique mass of this spectra
                */
              override val uniqueMass: Option[Double] = _uniqueMass
              /**
                * the local scan number
                */
              override val scanNumber: Int = x.scanNumber
              /**
                * the retention time of this spectra. It should be provided in seconds!
                */
              override val retentionTimeInSeconds: Double = _retentionTimeInSeconds
              /**
                * specified ion mode for the given feature
                */
              override val ionMode: Option[IonMode] = _ionMode
              /**
                * the associated complete scan for this feature
                */
              override val associatedScan: Option[SpectrumProperties] = _associatedScan
              /**
                * accurate mass of this feature, if applicable
                */
              override val massOfDetectedFeature: Option[Ion] = _massOfDetectedFeature
            }

          case x: MSSpectra =>
            val _sample = x.sample
            val _purity = x.purity
            val _signalNoise = x.signalNoise
            val _uniqueMass = x.uniqueMass
            val _scanNumber = x.scanNumber
            val _retentionTimeInSeconds = x.retentionTimeInSeconds
            val _ionMode = x.ionMode
            val _associatedScan = x.associatedScan match {
              case Some(p) =>
                Some(
                  new SpectrumProperties {
                    /**
                      * the msLevel of this spectra
                      */
                    override val msLevel: Short = p.msLevel
                    /*e
                      * a list of model ions used during the deconvolution
                      */
                    override val modelIons: Option[Seq[Double]] = p.modelIons
                    /**
                      * all the defined ions for this spectra
                      */
                    override val ions: Seq[Ion] = p.ions
                  }
                )

              case None => None
            }


            val _massOfDetectedFeature = x.massOfDetectedFeature


            new MSSpectra {
              /**
                * the associated sample
                */
              override val sample: String = _sample
              /**
                * how pure this spectra is
                */
              override val purity: Option[Double] = _purity
              /**
                * the signal noise of this spectra
                */
              override val signalNoise: Option[Double] = _signalNoise
              /**
                * the unique mass of this spectra
                */
              override val uniqueMass: Option[Double] = _uniqueMass
              /**
                * the local scan number
                */
              override val scanNumber: Int = _scanNumber
              /**
                * the retention time of this spectra. It should be provided in seconds!
                */
              override val retentionTimeInSeconds: Double = _retentionTimeInSeconds
              /**
                * specified ion mode for the given feature
                */
              override val ionMode: Option[IonMode] = _ionMode
              /**
                * the associated complete scan for this feature
                */
              override val associatedScan: Option[SpectrumProperties] = _associatedScan
              /**
                * accurate mass of this feature, if applicable
                */
              override val massOfDetectedFeature: Option[Ion] = _massOfDetectedFeature
            }
        }

        assert(wrapped_spectra.size == x.spectra.size, "you seem to be using none supported results!")

        logger.info(s"wrapped ${x.spectra.size} spectra into ${wrapped_spectra.size} ...")

        val _fileName = x.fileName
        val _properties = x.properties

        new Sample {

          /**
            * a collection of spectra
            * belonging to this sample
            */
          override val spectra: Seq[_ <: Feature] = wrapped_spectra
          /**
            * the unique file name of the sample
            */
          override val fileName: String = _fileName
          /**
            * associated properties
            */
          override val properties: Option[SampleProperties] = _properties
        }
    }
  }

  /**
    * gets all the specified samples
    *
    * @param names
    * @return
    */
  def getSamples(names: Seq[String]): Seq[_ <: Sample] = names.map(getSample)

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  def sampleExists(name: String): Boolean

}
