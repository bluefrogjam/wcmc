package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils

import java.io.{File, FileWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, MetadataSupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialLCMSProcessedSample
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class SampleSerializer() extends LazyLogging {

  @Value("${carrot.io.sample.destination:./}")
  val destination = ""

  @Autowired
  val objectMapper: ObjectMapper = null


  /**
    * saves the sample
    */
  def serialize(sample: Sample): Unit = {
    val file = new File(destination, s"${sample.name}_deco.json")
    val outstream: FileWriter = new FileWriter(file)
    val jsonStr: String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sample)

    outstream.write(jsonStr)
    outstream.flush()
    outstream.close()

    logger.debug(s"Saved ${jsonStr.size} bytes in ${destination}/${sample.name}_dump${sample.extension}")
  }


  def saveFile(sample: Sample): Unit = {
    val file = new File(destination, s"${sample.name}_deco.json")
    val writer: FileWriter = new FileWriter(file)

    writer.append("sample filename,").append("scan number,").append("ion mode,").append("mass of detected feature mz,").append("mass of detected feature int,").append("purity,").append("rt(s),").append("signal noise,").append("unique mass,").append("metadata").append("\n")

    sample.spectra.foreach {
      case spec: Feature with MetadataSupport =>
        writer.append(s"${sample.fileName},")
            .append(s"${spec.scanNumber},")
            .append(s"${spec.ionMode},")
            .append(s"${spec.massOfDetectedFeature.get.mass},")
            .append(s"${spec.massOfDetectedFeature.get.intensity},")
            .append(s"${spec.purity},")
            .append(s"${spec.retentionTimeInSeconds},")
            .append(s"${spec.uniqueMass},")
            .append(s"${spec.metadata.map(x => s"${x._1}=${x._2}").mkString(";")},")
            .append(s"${spec.associatedScan.get.ions.map(ion => s"${ion.mass}:${ion.intensity}").mkString(" ")}")
            .append(s"\n")
      case msspec: MSSpectra  =>
        writer.append(s"${sample.fileName},")
            .append(s"${msspec.scanNumber},")
            .append(s"${msspec.ionMode},")
            .append(s"${msspec.massOfDetectedFeature.getOrElse(Ion(0,0)).mass},")
            .append(s"${msspec.massOfDetectedFeature.getOrElse(Ion(0,0)).intensity},")
            .append(s"${msspec.purity},")
            .append(s"${msspec.retentionTimeInSeconds},")
            .append(s"${msspec.uniqueMass},")
            .append(s"no metadata,")
            .append(s"${msspec.associatedScan.get.ions.map(ion => s"${ion.mass}:${ion.intensity}").mkString(" ")}")
            .append(s"\n")
    }

    writer.flush()
    writer.close()
    logger.info(s"... finished.")
  }

}
