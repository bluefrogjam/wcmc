package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io._

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * write the result in form of a larger flat table, which can later be processed in different ways,
  * but it's still easy to be used in xls and so
  */
@Component
@Profile(Array("carrot.output.writer.flat"))
class FlatTableWriter[T] extends Writer[Sample] with Logging {

  @Value("${wcmc.workflow.lcms.output.writer.flat.separator:,}")
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

        val o = new PrintStream(outputStream)

        def sortedTargets = data.quantifiedTargets.sortBy(p => (p.retentionIndex, p.name))


        sortedTargets.zipWithIndex.foreach { quantifiedSpectra =>

          val target = quantifiedSpectra._1
          val feature = target.spectra

          o.print(sample.fileName)
          o.print(seperator)

          o.print(target.name.getOrElse(f"${target.retentionIndex}%1.2f_${target.precursorMass.getOrElse(0.0)}%1.4f"))
          o.print(seperator)

          o.print(data.featuresUsedForCorrection.exists(p => p.target == target))
          o.print(seperator)

          o.print(data.correctionFailed)
          o.print(seperator)

          o.print(
            if (feature.isDefined) {
              target match {
                case x: GapFilledTarget[T] => true
                case x: QuantifiedTarget[T] => false
              }
            } else {
              "FAILED"
            })

          o.print(seperator)
          o.print(target.retentionIndex)
          o.print(seperator)
          o.print(target.precursorMass.getOrElse(0.0))
          o.print(seperator)
          o.print(if (feature.isDefined) feature.get.retentionIndex else {
            0.0
          })
          o.print(seperator)
          o.print(if (feature.isDefined) feature.get.accurateMass.getOrElse(0.0) else {
            0.0
          })
          o.print(seperator)
          o.print(Math.abs(if (feature.isDefined) feature.get.retentionIndex - target.retentionIndex else {
            0.0
          }))
          o.print(seperator)
          o.print(Math.abs(if (feature.isDefined) (feature.get.accurateMass.getOrElse(0.0) - target.accurateMass.getOrElse(0.0)) * 1000 else {
            0.0
          }))

          o.print(seperator)
          o.print(if (feature.isDefined) MassAccuracy.calculateMassErrorPPM(feature.get, target).getOrElse(0.0) else {"FAILED"})

          o.print(seperator)
          o.print(if (feature.isDefined) feature.get.retentionTimeInSeconds else {
            0.0
          })
          o.print(seperator)
          o.print(if (feature.isDefined) feature.get.retentionTimeInMinutes else {
            0.0
          })
          o.print(seperator)
          o.print(if (feature.isDefined) {
            target.quantifiedValue.get match {
              case v: Double => if (v < 0.0) logger.warn(s"Negative intensity found for ${feature}")
            }
            target.quantifiedValue.get
          } else {
            0.0
          })
          o.println()
        }


        o.flush()
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

    val o = new PrintStream(outputStream)

    o.print("filename")
    o.print(seperator)
    o.print("target")
    o.print(seperator)
    o.print("found at correction")
    o.print(seperator)
    o.print("correction failed")
    o.print(seperator)

    o.print("replaced value")
    o.print(seperator)

    o.print("retention index (target)")
    o.print(seperator)
    o.print("mass (target)")
    o.print(seperator)
    o.print("retention index (annotation)")
    o.print(seperator)
    o.print("mass (annotation)")
    o.print(seperator)
    o.print("retention index shift")
    o.print(seperator)
    o.print("mass shift (mDa)")
    o.print(seperator)
    o.print("mass shift (ppm)")
    o.print(seperator)

    o.print("retention time (s)(annotation)")
    o.print(seperator)
    o.print("retention time (min)(annotation)")
    o.print(seperator)
    o.print("height (annotation)")
    o.println()
  }

  /**
    * the writers extension
    *
    * @return
    */
  override def extension: String = "csv"
}
