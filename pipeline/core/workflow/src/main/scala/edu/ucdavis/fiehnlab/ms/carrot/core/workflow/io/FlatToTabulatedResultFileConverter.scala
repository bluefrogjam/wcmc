package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{File, InputStream}
import java.nio.file.Files

import com.typesafe.scalalogging.LazyLogging
import joinery.DataFrame
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Converts a flat results file with target and sample columns to a samples vs. targets matrix
  *
  * Created by sajjan on 1/22/2018.
  */
@Component
class FlatToTabulatedResultFileConverter extends LazyLogging {

  private def format(x: Double, precision: Int, format: Boolean): String = {
    if (format) {
        s"%.${precision}f".format(x)
    } else {
        x.toString
    }
  }

  def convert(inputStream: InputStream, formatDecimals: Boolean = true): String = {
    var df = DataFrame.readCsv(inputStream)

    // Throw exception if file is not valid, ie, does not cotain required columns
    df = df.retain("filename", "target", "height (annotation)", "mass (target)", "retention index (target)")


    // Collect header data
    val filenames = ArrayBuffer[String]()
    val targets = ArrayBuffer[String]()
    val targetMasses = ArrayBuffer[Double]()
    val targetRetentionTimes = ArrayBuffer[Double]()

    (0 until df.length()).foreach { i =>
      val filename = df.get(i, "filename").toString
      val target = df.get(i, "target").toString

      if (!filenames.contains(filename)) {
        filenames += filename
      }

      if (!targets.contains(target)) {
        targets += target
        targetMasses += df.get(i, "mass (target)").toString.toDouble
        targetRetentionTimes += df.get(i, "retention index (target)").toString.toDouble
      }
    }

    // Construct data matrix
    val matrix = ArrayBuffer.fill(filenames.length, targets.length)("")

    (0 until df.length()).foreach { i =>
      val filename = df.get(i, "filename").toString
      val target = df.get(i, "target").toString

      matrix(filenames.indexOf(filename))(targets.indexOf(target)) = df.get(i, "height (annotation)").toString
    }

    // Construct output
    val buffer = new mutable.StringBuilder

    // Build header
    buffer ++= "target,"
    buffer ++= targets.mkString(",")
    buffer ++= "\n"
    buffer ++= "mass,"
    buffer ++= targetMasses.map(format(_, 4, formatDecimals)).mkString(",")
    buffer ++= "\n"
    buffer ++= "retention time (s),"
    buffer ++= targetRetentionTimes.map(format(_, 1, formatDecimals)).mkString(",")
    buffer ++= "\n"
    buffer ++= "retention time (min),"
    buffer ++= targetRetentionTimes.map(_ / 60).map(format(_, 2, formatDecimals)).mkString(",")
    buffer ++= "\n"

    // Build matrix
    filenames.zipWithIndex.foreach { case (filename, i) =>
      buffer ++= filename
      buffer ++= ","
      buffer ++= matrix(i).mkString(",")
      buffer ++= "\n"
    }

    buffer.toString()

    // Keeping as record in case it can be reused, but otherwise abandoning Joinery
    //    val filenames = df.col("filename").asScala.toSet
    //
    //    filenames.foreach { filename =>
    //      val predicate = new Predicate[Object] {
    //        override def apply(values: util.List[Object]): lang.Boolean = {
    //          values.get(0) == filename
    //        }
    //      }
    //
    //      var group = df.select(predicate)
    //      group = group.drop("filename")
    //      group = group.rename("height (annotation)", filename)
    //      group = group.rename("mass (target)", "mass")
    //      group = group.rename("retention index (target)", "retention time (s)")
    //      group = group.add("retention time (min)", group.col("retention time (s)").asScala.map(_.toString.toDouble / 60))
    //    }
  }
}
