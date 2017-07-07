package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.io

import java.io.InputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Reader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.{FocusedPeak, Peak}

import scala.io.Source

/**
  * Created by sajjan on 7/18/16.
  *
  */
class PeakListTxtReader(delimiter: String="\t") extends Reader[List[Peak]] with LazyLogging {

  /**
    * reads a new peak list
    *
    * @param inputStream
    * @return
    */
  override def read(inputStream: InputStream): List[Peak] = {

    /**
      * converts the data into a list of Peak objects
      */
    Source.fromInputStream(inputStream).getLines().collect {
      case line: String =>
        if (!line.startsWith("Scan#")) {
          val data = line.split(delimiter)
          FocusedPeak(data(0).toInt, data(1).toDouble, data(2).toDouble, data(3).toDouble)
        } else {
          null
        }
    }.filter(_ != null).toList
  }
}
