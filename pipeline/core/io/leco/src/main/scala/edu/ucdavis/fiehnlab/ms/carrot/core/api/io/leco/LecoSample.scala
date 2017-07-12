package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import java.io.{IOException, InputStream}

import com.typesafe.scalalogging.LazyLogging
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
    val lines: Iterator[String] = Source.fromInputStream(inputStream, "ISO-8859-1").getLines()

    if (lines.hasNext) {
      val headers = lines.next().toLowerCase().split("\t")
      var scan: Int = 0

      lines.collect {
        case line: String =>
          val contents = line.split("\t")
          val map = (headers zip contents).toMap

          scan = scan + 1
          buildSpectra(scan, map)
      }.toList
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
  def buildSpectra(scan: Int, map: Map[String, String]): LecoSpectrum = new LecoSpectrum {
    override val modelIons: Option[List[Double]] = Some(map.get(uniquemassIdentifier).get.toDouble :: List())
    override val purity: Option[Double] = Some(map.get(purityIdentifier).get.toDouble)
    override val scanNumber: Int = scan

    override val ions: List[Ion] = map.get(spectraIdentifier).get.toString.split(" ").collect {
      case x: String =>
        val values = x.split(":")

         Ion(values(0).toDouble, values(1).toFloat)

    }.filter(_.intensity > 0).toList
    override val retentionTimeInSeconds: Double = map.get(retentionTimeSecondsIdentifier).get.toDouble
    override val msLevel: Short = 1
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = None
  }
}
