package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import java.io.{IOException, InputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample, SampleProperties}
import org.apache.commons.io.IOUtils

import scala.io.Source

/**
  * presents a LECO ChromatTof processed sample
  */
class LecoSample(inputStream: InputStream, override val fileName: String) extends Sample with LazyLogging {

  val uniquemassIdentifier: String = "uniquemass"

  val signalNoiseIdentifier: String = "s/n"

  val purityIdentifier: String = "purity"

  val spectraIdentifier: String = "spectra"

  val retentionTimeSecondsIdentifier: String = "r.t. (s)"

  override val spectra: List[LecoSpectrum] = readFile(inputStream)

  /**
    * parse the given input stream and returns a list of spectra object
    *
    * @param inputStream
    * @return
    */
  def readFile(inputStream: InputStream): List[LecoSpectrum] = {
    try {

      val lines: Iterator[String] = Source.fromInputStream(inputStream, "ISO-8859-1").getLines().map(_.toLowerCase)
      logger.debug(s"we are having ${lines.size} spectra")
      if (lines.hasNext) {


        val first = lines.next()

        //extract the header
        val headers = if (first.isEmpty) lines.next().toLowerCase().trim.split("\t") else first.toLowerCase.trim.split("\t")
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
              case x: Throwable =>
                logger.warn(x.getMessage, x)
                logger.warn(s"line was: \n${line}\n")
                logger.warn(s"hearders are: \n${headers.mkString("\n")}\t")
                return null
            }
        }.filter(_ != null).toList
      }
      else {
        throw new IOException(s"sorry the file: ${fileName} contained no lines!")
      }
    }
    finally {
      IOUtils.closeQuietly(inputStream)
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

    LecoSpectrum(
      spectrum = Some(new SpectrumProperties {

        override val modelIons: Option[List[Double]] = Some(map(uniquemassIdentifier).replaceAll(",", ".").toDouble :: List())


        override val ions: List[Ion] = map(spectraIdentifier).toString.split(" ").collect {
          case x: String if x.split(":").size == 2 =>
            val values = x.split(":")

            Ion(values(0).toDouble, values(1).toFloat)

        }.filter(_.intensity > 0).toList
        /**
          * the msLevel of this spectra
          */
        override val msLevel: Short = 1
      }),
      sample = LecoSample.this.fileName,
      purity = Some(map(purityIdentifier).replaceAll(",", ".").toDouble),
      scanNumber = scan,
      retentionTimeInSeconds = map(retentionTimeSecondsIdentifier).replaceAll(",", ".").toDouble * 1000, //fix to deal with old BinBase RT time by factor 1000 issues
      uniqueMass = Some(map(uniquemassIdentifier).toDouble),
      signalNoise = Some(map(signalNoiseIdentifier).toDouble)


    )
  }

  /**
    * associated properties
    */
  override val properties: Option[SampleProperties] = None
}
