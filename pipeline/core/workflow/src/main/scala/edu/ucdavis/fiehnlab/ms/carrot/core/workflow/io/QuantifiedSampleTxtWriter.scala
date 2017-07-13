package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample, Target}

/**
  * writes the quantified sample as a txt file
  *
  * @tparam T
  */
class QuantifiedSampleTxtWriter[T](seperator: String = "\t", noneReplacements: String = "NA") extends Writer[Sample] with LazyLogging {

  var lineCounter: Int = 0


  /**
    * writes the given sample to the output stream
    *
    * @param outputStream
    * @param sample
    */
  override def write(outputStream: OutputStream, sample: Sample): Unit = {
    logger.debug(s"writing sample: ${sample}")

    sample match {

      /**
        * we have a quantified sample
        */
      case data: QuantifiedSample[T] =>

        val out = new BufferedWriter(new OutputStreamWriter(outputStream))

        def sortedTargets = data.quantifiedTargets.sortBy(p => (p.retentionTimeInSeconds, p.name))

        //writes the header
        if (lineCounter == 0) {

          def writeLine(header: String, value: (Target) => String) = {
            out.write(header)
            out.write(seperator)

            //write target list
            sortedTargets.zipWithIndex.foreach { quantifiedSpectra =>
              val target = quantifiedSpectra._1


              out.write(value(target))

              if (quantifiedSpectra._2 < data.quantifiedTargets.size - 1) {
                out.write(seperator)
              }
              else {
                out.write("\n")
              }
            }

          }


          writeLine("target", target => target.name.getOrElse(target.retentionTimeInSeconds).toString)
          writeLine("mass", target => f"${target.monoIsotopicMass.getOrElse(0.0)}%1.4f")
          writeLine("retention time (s)", target => f"${target.retentionTimeInSeconds}%1.2f")
          writeLine("retention time (min)", target => f"${target.retentionTimeInMinutes}%1.2f")
          //write mass

          //write retention time of target
        }

        //writes the actual data
        out.write(data.fileName)
        out.write(seperator)

        sortedTargets.zipWithIndex.foreach { quantifiedSpectra =>
          out.write(quantifiedSpectra._1.quantifiedValue.getOrElse(noneReplacements).toString)

          if (quantifiedSpectra._2 < data.quantifiedTargets.size - 1) {
            out.write(seperator)
          }
          else
          {
            out.write("\n")
          }
        }


        out.flush()
    }
    lineCounter = lineCounter + 1

  }

  /**
    * rests the line counter
    *
    * @param outputStream
    */
  override def writeHeader(outputStream: OutputStream) = {
    lineCounter = 0
  }
}
