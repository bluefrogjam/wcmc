package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import java.io.{IOException, InputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample}

import scala.io.Source

/**
  * presents a LECO ChromatTof processed sample
  */
class LecoSample(inputStream: InputStream, override val fileName: String) extends Sample with LazyLogging {

  override val spectra: List[LecoSpectrum] = readFile(inputStream)

  def uniquemassIdentifier: String = "uniquemass"

  def purityIdentifier: String = "purity"

  def spectraIdentifier: String = "spectra"

  def retentionTimeSecondsIdentifier: String = "r.t. (s)"

  /**
    * parse the given input stream and returns a list of spectra object
    *
    * @param inputStream
    * @return
    */
  def readFile(inputStream: InputStream): List[LecoSpectrum] = {
    val lines: Iterator[String] = Source.fromInputStream(inputStream, "ISO-8859-1").getLines().map(_.toLowerCase)

    if (lines.hasNext) {


      val first = lines.next()

      //extract the header
      val headers = if ( first.isEmpty )lines.next().toLowerCase().split("\t") else first.toLowerCase.split("\t")
      var scan: Int = 0

      lines.collect {
        case line: String =>
          val contents = line.split("\t")
          val map = (headers zip contents).toMap

          scan = scan + 1
          try {
            buildSpectra(scan, map)
          }
          catch {
            case x: Exception =>
              logger.warn(x.getMessage, x)
              return null
          }
      }.filter(_ != null).toList
    }
    else {
      throw new IOException(s"sorry the file: ${fileName} contained no lines!")
    }
  }

  /**
    * assembles a spectra, based on the provided read line
    *
    * @param scan
    * @param map
    * @return
    */
  def buildSpectra(scan: Int, map: Map[String, String]): LecoSpectrum = {

    val spec = new LecoSpectrum {

      val sample: Sample = LecoSample.this
      override val purity: Option[Double] = Some(map(purityIdentifier).replaceAll(",", ".").toDouble)
      override val scanNumber: Int = scan

      override val retentionTimeInSeconds: Double = map(retentionTimeSecondsIdentifier).replaceAll(",", ".").toDouble * 1000 //fix to deal with old BinBase RT time by factor 1000 issues
      /**
        * accurate mass of this feature, if applicable
        */
      override val massOfDetectedFeature: Option[Ion] = None

      override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {
        override val modelIons: Option[List[Double]] = Some(map(uniquemassIdentifier).replaceAll(",", ".").toDouble :: List())


        override val ions: List[Ion] = map(spectraIdentifier).toString.split(" ").collect {
          case x: String =>
            val values = x.split(":")

            Ion(values(0).toDouble, values(1).toFloat)

        }.filter(_.intensity > 0).toList
        /**
          * the msLevel of this spectra
          */
        override val msLevel: Short = 1
      })
      /**
        * associated spectrum propties if applicable
        */
      override lazy val spectrum: Option[SpectrumProperties] = associatedScan
    }
    spec
  }
}
