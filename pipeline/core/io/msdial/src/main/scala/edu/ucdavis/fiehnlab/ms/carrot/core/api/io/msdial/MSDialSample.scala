package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial

import java.io.{File, FileInputStream, IOException, InputStream}
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample}

import scala.io.Source

/**
  * creates a new msdial sample from the given file
  */
object MSDialSample {
  def apply(name:String,file: File): MSDialSample = {
    if(file.getName.endsWith("gz")){
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

	/*
	 * msdial's output:
	 * PeakID  Title   Scans   RT(min) Precursor m/z   Height  Area    Model Masses MetaboliteName  AdductIon       Isotope SMILES  InChIKey        Dot product     Reverse dot product      Fragment presence %     Total score     MS1 spectrum    MSMS spectrum
	 */

  protected val scanIdentifier: String = "peakid"
  protected val nameIdentifier: String = "title"
	protected val numberOfScansIdentifier: String = "scans"
  protected val retentionTimeMinutesIdentifier: String = "rt(min)"
	protected val precursorMZIdentifier: String = "precursor m/z"
  protected val intensityIdentifier: String = "height"
	protected val areaIdentifier = "area"
  protected val purityIdentifier: String = "metabolitename"
  protected val adductIonNameIdentifier: String = "adductionname"
  protected val isotopeIdentifier: String = "isotope"
  protected val smilesIdentifier: String = "smiles"
  protected val inchikeyIdentifier: String = "inchikey"
  protected val dotProductIdentifier: String = "dot product"
  protected val reverseDotProductIdentifier: String = "reverse dot product"
  protected val fragmentPrecensePercentIdentifier: String = "fragment presence %"
  protected val totalScoreIdentifier: String = "total score"
  protected val spectrumIdentifier: String = "ms1 spectrum"
  protected val msmsSpectrumIdentifier: String = "msms spectrum"

	protected val modelMassesIdentifier: String = "model masses"

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
	    logger.debug(s"Headers: ${headers.mkString(" - ")}")
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

	  if (!dataMap.keySet.contains(spectrumIdentifier)) {
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

	      // WHY the mass IS an ION ????
	      override val massOfDetectedFeature: Option[Ion] = Option(Ion(dataMap(precursorMZIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))
        /**
          * how pure this spectrum is
          */
        override val purity: Option[Double] = None
        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = None
      }
	  } else if (!dataMap.contains(msmsSpectrumIdentifier)) {

      /**
        * complete spectra available
        */
      new MSSpectra {

        override val massOfDetectedFeature: Option[Ion] = Option(Ion(dataMap(precursorMZIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))

        override val modelIons: Option[List[Double]] = Option(dataMap(modelMassesIdentifier).split(",").filter(_.nonEmpty).map(_.toDouble).toList)

        override val scanNumber: Int = dataMap(scanIdentifier).toInt

        override val ions: Seq[Ion] = dataMap(spectrumIdentifier).split(" ").filter(_.nonEmpty).collect {
            case x: String if x.nonEmpty =>
              val values = x.split(":")

              Ion(values(0).toDouble, values(1).toFloat)

          }.filter(_.intensity > 0).toSeq


        override val retentionTimeInSeconds: Double = dataMap(retentionTimeMinutesIdentifier).toDouble * 60

	      override val msLevel: Short = 1

        override val purity: Option[Double] = None
        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = None
      }
	  } else {
		  new MSMSSpectra {
			  /* PeakID  Title   Scans   RT(min) Precursor m/z   Height  Area    MetaboliteName  AdductIon       Isotope SMILES  InChIKey        Dot product     Reverse dot product      Fragment presence %     Total score     MS1 spectrum    MSMS spectrum */
			  override val massOfDetectedFeature: Option[Ion] = Option(Ion(dataMap(precursorMZIdentifier).toDouble, dataMap(intensityIdentifier).toFloat))

			  override val modelIons: Option[List[Double]] = Option(dataMap(modelMassesIdentifier).split(",").filter(_.nonEmpty).map(_.toDouble).toList)

			  override val scanNumber: Int = dataMap(scanIdentifier).toInt

			  override val ions: Seq[Ion] = dataMap(msmsSpectrumIdentifier).split(" ").filter(_.nonEmpty).collect {
				  case x: String if x.nonEmpty =>
					  val values = x.split(":")

					  Ion(values(0).toDouble, values(1).toFloat)

			  }.filter(_.intensity > 100).toSeq


			  override val retentionTimeInSeconds: Double = dataMap(retentionTimeMinutesIdentifier).toDouble * 60

			  override val msLevel: Short = 2

			  override val purity: Option[Double] = if (dataMap(fragmentPrecensePercentIdentifier).isEmpty) None else Option(dataMap(fragmentPrecensePercentIdentifier).toDouble)
			  /**
				  * specified ion mode for the given feature
				  */
			  override val ionMode: Option[IonMode] = None
			  /**
				  * the observed pre cursor ion
				  */
			  override val precursorIon: Double = dataMap(precursorMZIdentifier).toDouble
		  }
	  }
  }
}

trait DeconvolutedSample {}
