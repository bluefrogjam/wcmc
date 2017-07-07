package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial

import java.io.{File, FileInputStream, IOException, InputStream}
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample}

import scala.io.Source
import scala.util.Try

/**
  * creates a new msdial sample from the given file
  */
object MSDialSample {
  def apply(file: File): MSDialSample = {
    if(file.getName.endsWith("gz")){
      new MSDialSample(new GZIPInputStream(new FileInputStream(file)), file.getName)
    }
    else {
      new MSDialSample(new FileInputStream(file), file.getName)
    }
  }
}

/**
  *
  * @param inputStream
  * @param fileName
  */
class MSDialSample(inputStream: InputStream, override val fileName: String) extends Sample with LazyLogging {

  val nameIdentifier: String = "name"
  val scanLeftIdentifier: String = "scanatleft"
  val scanIdentifier: String = "scanattop"
  val scanRightIdentifier: String = "scanatright"
  val retentionTimeLeftMinutesIdentifier: String = "rtatleft(min)"
  val retentionTimeMinutesIdentifier: String = "rtattop(min)"
  val retentionTimeRightMinutesIdentifier: String = "rtatright(min)"
  val intensityAtLeftIdentifier: String ="heightatleft"
  val intensityIdentifier: String = "heightattop"
  val intensityAtRightIdentifier: String = "heightatright"
  val areaAboveZeroIdentifier: String = "areaabovezero"
  val areaAboveBaselineIdentifier: String = "areaabovecaseline"
  val normalizedValueIdentifier: String = "normalizednalue"
  val mPlus1IonIntensityIdentifier: String = "ms1_m+1intensity"
  val mPlus2IonIntensityIdentifier: String = "ms1_m+2intensity"
  val purityIdentifier: String = "peakpurevalue"
  val sharpnessIdentifier: String = "sharpness"
  val gaussianSimilarityIdentifier: String = "gaussiansimylarity"
  val idealSlopeIdentifier: String = "idealslope"
  val modelMassesIdentifier: String = "modelmasses"
  val uniquemassIdentifier: String = "uniquemass"
  val basePeakIdentifir: String = "basepeakvalue"
  val symmetryIdentifier: String = "symmetry"
  val amplitudeScoreIdentifier: String = "amplitudescore"
  val amplitudOrderIdentifier: String = "amplitudorder"
  val adductIonNameIdentifier: String = "adductionname"
  val adductParentIdentifier: String = "adductparent"
  val adductIonAccurateMassIdentifier: String = "adductionaccuratemass"
  val adductIonXmerIdentifier: String = "adductionxmer"
  val adductIonChargeIdentifier: String = "adductionchargenumber"
  val accurateMassIdentifier: String = "accuratemass"
  val accurateMassSimilarityIdentifier: String = "accuratemasssimilarity"
  val isotopeIdentifier: String = "isotope"
  val dotProductIdentifier: String = "dot product"
  val reverseDotProductIdentifier: String = "reverse dot product"
  val fragmentPrecensePercentIdentifier: String = "fragment presence %"
  val totalScoreIdentifier: String = "total score"
  val spectraIdentifier: String = "spectra"

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

	      override val accurateMass: Option[Ion] = Option(Ion(dataMap(accurateMassIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))
      }
	  } else {

      /**
        * complete spectra available
        */
      new MSSpectra {

        override val accurateMass: Option[Ion] = Option(Ion(dataMap(accurateMassIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))

        override val modelIons: Option[List[Double]] = Some(dataMap(modelMassesIdentifier).split(",").filter(_.nonEmpty).map(_.toDouble).toList)

        override val scanNumber: Int = dataMap(scanIdentifier).toInt

        /**
          * if no spectra is defined, we have the cxurrent peak mz:intensity
          * to have a spectrum for correction
          * BUG
          */
        override val ions: Seq[Ion] = dataMap(spectraIdentifier).split(" ").filter(_.nonEmpty).collect {
            case x: String if x.nonEmpty =>
              val values = x.split(":")

              Ion(values(0).toDouble, values(1).toFloat)

          }.filter(_.intensity > 0).toSeq


        override val retentionTimeInSeconds: Double = dataMap(retentionTimeMinutesIdentifier).toDouble * 60

        override val msLevel: Short = 1

        //TODO resolve into something, purity should be calculated across scans, not on a single spectrum
        override val purity: Option[Double] = Some(Try(dataMap(purityIdentifier).toDouble).getOrElse(-1.0))

      }
    }
  }
}

trait DeconvolutedSample {}
