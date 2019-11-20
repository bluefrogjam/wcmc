package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.OutputStream

import com.lambdaworks.jacks.JacksMapper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter.SampleConverter
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.ResultData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * writes the quantified sample as a json file
  *
  * @tparam T
  */
@Component
@Profile(Array("carrot.output.writer.json"))
class QuantifiedSampleJsonWriter[T] @Autowired()(converter: SampleConverter[Double, ResultData]) extends Writer[Sample] with Logging {

  /**
    * writes the footer, if supported
    *
    * @param outputStream
    */
  override def writeFooter(outputStream: OutputStream): Unit = {}

  /**
    * writes the given sample to the output stream
    *
    * @param outputStream
    * @param sample
    */
  override def write(outputStream: OutputStream, sample: Sample): Unit = {
    sample match {

      case quantified: QuantifiedSample[Double] =>
        logger.info(s"writing sample: ${sample.name}")
        JacksMapper.writeValue(outputStream, converter.convert(quantified))
    }

  }

  /**
    * rests the line counter
    *
    * @param outputStream
    */
  override def writeHeader(outputStream: OutputStream): Unit = {}

  /**
    * the writers extension
    *
    * @return
    */
  override def extension: String = "json"
}
