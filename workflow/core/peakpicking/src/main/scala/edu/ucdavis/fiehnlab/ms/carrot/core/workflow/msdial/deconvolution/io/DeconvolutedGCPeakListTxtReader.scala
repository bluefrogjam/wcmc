package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.io

import java.io.InputStream
import java.lang.{Double => JDouble}
import java.util.{ArrayList => JArrayList}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Reader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.MS1DecResult
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._
import scala.io.Source

/**
  * Created by diego on 8/17/2016.
  */
@Component
class DeconvolutedGCPeakListTxtReader(delimiter: String) extends Reader[List[MS1DecResult]] with LazyLogging {

  def this() = this("\t")

  /**
    * reads a list of MS-Dial MS1DecResult objects exported as txt
    *
    * @param inputStream
    * @return
    */
  override def read(inputStream: InputStream): List[MS1DecResult] = {

    val lines = Source.fromInputStream(inputStream).getLines().toIndexedSeq

    /**
      * converts the data into a list of MS1DecResult objects
      */
    lines.map { line: String =>
      line.trim match {
        case x if !x.toLowerCase().startsWith("Scan#".toLowerCase()) =>
          val data = x.split(delimiter)

          def getToken(data: String, index: Int, delim: Char): String = {
            data.split(delim)(index)
          }

          val mm = data(24).replaceAll("[\\[\\]]", "") match {
            case "" => new JArrayList[JDouble]()
            case _ => data(24).replaceAll("[\\[\\]]", "").split(",").filter(_ != "")
              .map(s => JDouble.valueOf(s)).toList.asJava
          }

          val ms = data(25).replaceAll("[\\[\\]]", "") match {
            case "" => new JArrayList[MsDialPeak]()
            case _ => data(25).replaceAll("[\\[\\]]", "").split("; ").filter(_ != "")
              .map(s => new MsDialPeak(data(0).toInt, data(1).toDouble, s.replaceAll("[\\(\\)]", "").split(":")(0).toDouble, s.replaceAll("[\\(\\)]", "").split(":")(1).toDouble)).toList.asJava
          }

          val bc = data(26).replaceAll("[\\[\\]]", "") match {
            case "" => new JArrayList[MsDialPeak]()
            case _ => data(26).replaceAll("[\\[\\]]", "").split("; ").filter(_ != "")
              .map(s => {
                val bpc = s.replaceAll("[\\(\\)]", "").split(",")
                new MsDialPeak(bpc(0).toInt, bpc(1).toDouble, -1, -1, bpc(3), bpc(4).toBoolean)
              }
              ).toList.asJava
          }

          val res = new MS1DecResult()
          res.scanNumber = data(0).toInt
          res.retentionTime = data(1).toDouble
          res.ms1DecID = data(3).toInt
          res.basepeakMz = data(4).toDouble
          res.basepeakArea = data(5).toDouble
          res.basepeakHeight = data(6).toDouble
          res.integratedArea = data(7).toDouble
          res.integratedHeight = data(8).toDouble
          res.modelPeakPurity = data(9).toDouble
          res.modelPeakQuality = data(10).toDouble
          res.amplitudeScore = data(12).toDouble
          res.signalNoiseRatio = data(17).toDouble
          res.splash = data(19)
          res.modelMasses = mm
          res.spectrum = ms
          res.baseChromatogram = bc

          Option(res)
        case _ =>
          None
      }
    }.filter(_.isDefined).map(_.get).toList
  }
}
