package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * writes the quantified sample as a txt file
  *
  * @tparam T
  */
@Component
@Profile(Array("carrot.output.writer.txt"))
class QuantifiedSampleTxtWriter[T] extends Writer[Sample] with LazyLogging {

  @Value("${carrot.output.writer.txt.separator:,}")
  val seperator: String = ","

  var lineCounter: Int = 0


  /**
    * writes the footer, if supported
    *
    * @param outputStream
    */
  override def writeFooter(outputStream: OutputStream): Unit = super.writeFooter(outputStream)

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

        def sortedTargets = data.quantifiedTargets.sortBy(p => (p.retentionIndex, p.name))

        //writes the header
        if (lineCounter == 0) {

          def writeLine(header: String, value: Target => String) = {
            logger.trace(s"writing header: ${header}")
            out.write(header)
            out.write(seperator)

            //write target list
            sortedTargets.zipWithIndex.foreach { quantifiedSpectra =>
              val target = quantifiedSpectra._1

              logger.trace(s"writing value: ${value(target)}")
              out.write(value(target))

              if (quantifiedSpectra._2 < data.quantifiedTargets.size - 1) {
                out.write(seperator)
              }
              else {
                out.write("\n")
              }
            }

          }


          writeLine("target", target => target.name.getOrElse(f"${target.retentionIndex}%1.2f_${target.precursorMass.getOrElse(0.0)}%1.4f").toString)
          writeLine("mass", target => f"${target.precursorMass.getOrElse(0.0)}%1.4f")
          writeLine("retention time (s)", target => f"${target.retentionIndex}%1.2f")
          writeLine("retention time (min)", target => f"${target.retentionTimeInMinutes}%1.2f")
          //write mass

          //write retention time of target
        }

        //writes the actual data
        out.write(data.fileName)
        out.write(seperator)

        sortedTargets.zipWithIndex.foreach { quantifiedSpectra =>

          val target = quantifiedSpectra._1

          //studid way to format numbers, scala give me a decent class hirachy for numbers...
          val res: String = if (target.quantifiedValue.isDefined) {
            target match {
              case x: GapFilledTarget[T] =>
                val data: String = s"${x.quantifiedValue.get}"

                try {
                  f"[${data.toDouble}%1.0f]"
                }
                catch {
                  case n: NumberFormatException => s"[$n]"
                }
              case x: QuantifiedTarget[T] =>
                val data: String = s"${x.quantifiedValue.get}"

                try {
                  f"${data.toDouble}%1.0f"
                }
                catch {
                  case n: NumberFormatException => data
                }
            }
          }
          else {
            "[NA]"
          }

          out.write(res)

          if (quantifiedSpectra._2 < data.quantifiedTargets.size - 1) {
            out.write(seperator)
          }
          else {
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

  /**
    * the writers extension
    *
    * @return
    */
  override def extension: String = "csv"
}
