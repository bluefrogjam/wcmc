package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial

import java.io.{File, FileInputStream, IOException, InputStream}
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample, Unknown}

import scala.io.Source

/**
	* creates a new msdial sample from the given file
	*/
object MSDialSample {
  def apply(name: String, file: File): MSDialSample = {
    if (file.getName.endsWith("gz")) {
      new MSDialSample(new GZIPInputStream(new FileInputStream(file)), name)
    }
    else {
      new MSDialSample(new FileInputStream(file), name)
    }
  }
}

/**
	*
	* @param inputStream
	* @param fileName
	*/
class MSDialSample(inputStream: InputStream, override val fileName: String) extends Sample with LazyLogging {

  protected val nameIdentifier: String = "name"
  protected val scanLeftIdentifier: String = "scanatleft"
  protected val scanIdentifier: String = "scanattop"
  protected val scanRightIdentifier: String = "scanatright"
  protected val retentionTimeLeftMinutesIdentifier: String = "rtatleft(min)"
  protected val retentionTimeMinutesIdentifier: String = "rtattop(min)"
  protected val retentionTimeRightMinutesIdentifier: String = "rtatright(min)"
  protected val intensityAtLeftIdentifier: String = "heightatleft"
  protected val intensityIdentifier: String = "heightattop"
  protected val intensityAtRightIdentifier: String = "heightatright"
  protected val areaAboveZeroIdentifier: String = "areaabovezero"
  protected val areaAboveBaselineIdentifier: String = "areaabovecaseline"
  protected val normalizedValueIdentifier: String = "normalizednalue"
  protected val mPlus1IonIntensityIdentifier: String = "ms1_m+1intensity"
  protected val mPlus2IonIntensityIdentifier: String = "ms1_m+2intensity"
  protected val purityIdentifier: String = "peakpurevalue"
  protected val sharpnessIdentifier: String = "sharpness"
  protected val gaussianSimilarityIdentifier: String = "gaussiansimylarity"
  protected val idealSlopeIdentifier: String = "idealslope"
  protected val modelMassesIdentifier: String = "modelmasses"
  protected val uniquemassIdentifier: String = "uniquemass"
  protected val basePeakIdentifir: String = "basepeakvalue"
  protected val symmetryIdentifier: String = "symmetry"
  protected val amplitudeScoreIdentifier: String = "amplitudescore"
  protected val amplitudOrderIdentifier: String = "amplitudorder"
  protected val adductIonNameIdentifier: String = "adductionname"
  protected val adductParentIdentifier: String = "adductparent"
  protected val adductIonAccurateMassIdentifier: String = "adductionaccuratemass"
  protected val adductIonXmerIdentifier: String = "adductionxmer"
  protected val adductIonChargeIdentifier: String = "adductionchargenumber"
  protected val accurateMassIdentifier: String = "accuratemass"
  protected val accurateMassSimilarityIdentifier: String = "accuratemasssimilarity"
  protected val isotopeIdentifier: String = "isotope"
  protected val dotProductIdentifier: String = "dot product"
  protected val reverseDotProductIdentifier: String = "reverse dot product"
  protected val fragmentPrecensePercentIdentifier: String = "fragment presence %"
  protected val totalScoreIdentifier: String = "total score"
  protected val spectraIdentifier: String = "spectra"

	override val spectra: Seq[Feature] = readFile(inputStream)

  /**
    * parse the given input stream and returns a list of spectra object
    *
    * @param inputStream
    * @return
    */
  def readFile(inputStream: InputStream): Seq[Feature] = {
    val lines: Iterator[String] = Source.fromInputStream(inputStream, "ISO-8859-1").getLines()

    if (lines.hasNext) {
      val lineh = lines.next()
      val headers = lineh.toLowerCase().split("\t").toList

      lines.collect {
        case line: String if line nonEmpty =>
          val contents = line.split("\t").toList
          val map = (headers zip contents).toMap

          buildSpectra(map)
      }.filter(_ != null).toSeq
    } else {
      throw new IOException(s"sorry the file: $fileName contained no lines!")
    }
  }

	/**
		* assembles a spectra, based on the provided read line
		*
		* @param dataMap
		* @return
		*/
	def buildSpectra(dataMap: Map[String, String]): Feature = {

    logger.debug(s"datamap: ${dataMap}")
    if (!dataMap.keySet.contains(spectraIdentifier)) {
      /**
        * no spectra available so it's just a feature
        */
      new Feature {
        /**
          * the retention time of this spectra. It should be provided in seconds!
          */
        override val retentionTimeInSeconds: Double = dataMap(retentionTimeMinutesIdentifier).toDouble * 60
        /**
          * the local scan number
          */
        override val scanNumber: Int = dataMap(scanIdentifier).toInt

        override val massOfDetectedFeature: Option[Ion] = Option(Ion(dataMap(accurateMassIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))
        /**
          * how pure this spectra is
          */
        override val purity: Option[Double] = None
        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = None
      }
    } else {

      /**
        * complete spectra available
        */
      new MSMSSpectra {

        override val massOfDetectedFeature: Option[Ion] = Option(Ion(dataMap(accurateMassIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))

        override val scanNumber: Int = dataMap(scanIdentifier).toInt


        override val retentionTimeInSeconds: Double = dataMap(retentionTimeMinutesIdentifier).toDouble * 60

        override val purity: Option[Double] = None
        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = None
        /**
          * the observed pre cursor ion. Assssumed to be the accurateMasssIdentifier
          */
        override val precursorIon: Double = dataMap(accurateMassIdentifier).toDouble

        override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {

          override val msLevel: Short = 2

          override val modelIons: Option[List[Double]] = Some(dataMap(modelMassesIdentifier).split(",").filter(_.nonEmpty).map(_.toDouble).toList)

          override val ions: Seq[Ion] = dataMap(spectraIdentifier).split(" ").filter(_.nonEmpty).collect {
            case x: String if x.nonEmpty =>
              val values = x.split(":")

              Ion(values(0).toDouble, values(1).toFloat)

          }.filter(_.intensity > 0).toSeq

        })

      }
    }
  }
}

trait DeconvolutedSample {}
