package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.InputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{Reader, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import scala.io.Source

/**
  * Created by wohlgemuth on 7/5/16.
  *
  * generates an experiment from the given layout file, which is really simple
  *
  * filename \t class
  *
  */
@Component
class ExperimentTXTReader @Autowired()(val loader: SampleLoader, val properties: ExperimentReaderTxTProperties) extends Reader[Experiment] with LazyLogging {

  /**
    * reads a new sample
    *
    * @param inputStream
    * @return
    */
  override def read(inputStream: InputStream): Experiment = {

    /**
      * converts the data, groups by class and assemble as an experiment
      */
    val result = Experiment(Source.fromInputStream(inputStream).getLines().toSeq.collect {
      case line: String =>
        if (!line.startsWith("#")) {
          val data = line.split(properties.delimiter)
          val sample = loader.loadSample(data(0)).get

          if (data.size == 1) {
            ("none", sample)
          }
          else {
            (data(1), sample)
          }
        }
        else {
          null
        } // returns a tuple (int, proxySample) proxySample = Sample to be loaded
    }.seq // make a sequence of streams in experiment file
      .filter(_ != null) // leave nulls out
      .groupBy(k => k._1) // group by class index (from experiment file)
      .mapValues(v => v.map(_._2)) // for each group extract the filename from the tuple (created in groupBy)
      .map(tuple => ExperimentClass(tuple._2, Some(new Matrix(tuple._1, "None", "None", Seq.empty)))).toSeq, acquisitionMethod = AcquisitionMethod()) // for each value create an ExperimentClass from the tuple data

    if (result.classes.isEmpty) throw new RuntimeException("no classes for the experiment are defined!")

    result.classes.foreach { clazz =>
      if (clazz.samples.isEmpty) throw new RuntimeException("no samples are defined!")
    }

    result
  }
}

@Component
@ConfigurationProperties(prefix = "experiment.txt.reader")
class ExperimentReaderTxTProperties {
  var delimiter: String = ";"
}
