package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * Created by diego on 11/9/2016.
  */

class SampleTXTWriter(separator: String = "\t", noneReplacements: String = "NA") extends Writer[Sample] with LazyLogging {
  var lineCounter: Int = 0

  override def write(outputStream: OutputStream, sample: Sample): Unit = {
    logger.debug(s"writing sample: ${sample.getClass.getSimpleName}")

    val out = new BufferedWriter(new OutputStreamWriter(outputStream))

    sample match {
//      case data: MSDialSample =>
      case data: Sample =>

        def sortedSpectra = data.spectra.sortBy(p => p.retentionTimeInSeconds)

        //writes the actual data
        out.write(data.fileName)
        out.write("\n")
        out.write("Scan#\tRT(min)\tBase Peak MZ\tBase Peak Int\tPurity\tSpectrum")
        out.write("\n")

        sortedSpectra.foreach {
          case decSpec: MSSpectra =>
            val line = f"${decSpec.scanNumber}%d\t${decSpec.retentionTimeInMinutes}%1.5f\t${decSpec.spectrum.get.basePeak.mass}%1.5f\t${decSpec.spectrum.get.basePeak.intensity}%1.5f\t${decSpec.purity.getOrElse(-1.0)}%1.5f\t${decSpec.spectrum.get.ions.mkString(" ")}%s\n"
            out.write(line)
          case _ =>
            logger.debug("not a spectra, was a feature!")
        }

        out.flush()

//      case _ =>
//        out.write(sample.getClass.toString)
//        out.write("\n")
//        out.flush()
    }
  }

  /**
    * the writers extension
    *
    * @return
    */
  override def extension: String = "txt"
}
