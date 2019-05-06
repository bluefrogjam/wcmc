package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import java.io.{File, IOException}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action.charting.BinBaseXYDataSet
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.{ChartUtils, JFreeChart}
import org.springframework.beans.factory.annotation.Autowired


// commented since its not used, need to fix ion extraction from raw and corrected data
//@Profile(Array("charting"))
//@Component
class ChartingAction[T](@Autowired loader: SampleLoader) extends PostAction {
  val halfRTDelta: Int = 12
  var rawData: Sample = _
  var rawfolder: File = _
  var correctedfolder: File = _

  override def run(ssample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Unit = {
    rawfolder = new File(s"./charts/raw/${experiment.acquisitionMethod.chromatographicMethod.ionMode.get.mode}")
    correctedfolder = new File(s"./charts/corrected/${experiment.acquisitionMethod.chromatographicMethod.ionMode.get.mode}")

    if (!rawfolder.exists) {
      println("creating folder " + rawfolder)
      rawfolder.mkdirs
    }
    if (!correctedfolder.exists) {
      println("creating folder " + correctedfolder)
      correctedfolder.mkdirs
    }

    ssample match {
      case value: QuantifiedSample[T] =>
        rawData = loader.getSample(ssample.fileName)

        value.quantifiedTargets.foreach(target => {
          println(target.name)
          createChart(target, rawfolder, ssample.asInstanceOf[QuantifiedSample[T]])
        })

      case _ => None
    }
  }

  private def cleanName(dirty: String) = {
    dirty.replaceAll("_[A-Z]{14}-[A-Z]{10}-[A-Z]|/|\\*\\\\|\\|", "")
        .replaceAll("\\(", "[")
        .replaceAll("\\)", "]")
        .replaceAll(":", "_")
  }

  def createLineChart(data: BinBaseXYDataSet, title: String): JFreeChart = {
    val rendererShift = new XYLineAndShapeRenderer(true, false)
    val x = new NumberAxis("retention time")
    x.setAutoRangeIncludesZero(false)

    val y = new NumberAxis("intensity")
    val plot = new XYPlot(data, x, y, rendererShift)
    val chart = new JFreeChart(title, plot)

    chart.setAntiAlias(false)
    chart
  }


  def createChart(target: Target, dest: File, qsample: QuantifiedSample[T]): Unit = {
    val dataset = new BinBaseXYDataSet
    val tgtId = cleanName(target.name.get).concat(f"_${target.accurateMass.get}%.4f")

    val start = target.retentionTimeInSeconds - halfRTDelta
    val end = target.retentionTimeInSeconds + halfRTDelta

    val rts = Seq[Double]()
    val ints = Seq[Double]()

    rawData.spectra.foreach(it => {
      print(f"feature ${tgtId} ${it.retentionTimeInMinutes}%.4f ${it.accurateMass.getOrElse(0.0)}%.4f")
      if (it.retentionTimeInSeconds >= start && it.retentionTimeInSeconds < end) {
        val ion = MassAccuracy.findClosestIon(it, target.accurateMass.get, target)
        if (ion.isDefined) {
          rts :+ it.retentionTimeInMinutes
          ints :+ ion.get.intensity.toDouble
        } else {
          println(" no close ions")
        }
      }
    })

    assert(rts.size == ints.size)
    dataset.addDataSet(rts.toArray, ints.toArray, tgtId)

    //corrected spectra dataset
    //    qsample.spectra.foreach((spec: _ <: Feature with CorrectedSpectra) => {
    //      println(spec)
    //    })


    // correcting rts for corrected plot
    val crts = rts.map(rt => qsample.regressionCurve.computeY(rt))
    dataset.addDataSet(crts.toArray, ints.toArray, s"corrected ${tgtId}")

    // add target focused rt marker & corrected rt marker
    val intBarTgt = try {
      ints.max match {
        case 0 => 10000
        case value => value
      }
    } catch {
      case ex: UnsupportedOperationException => 10000
    }
    val rtBarTgt = target.retentionTimeInSeconds
    dataset.addDataSet(Array[Double](rtBarTgt, rtBarTgt), Array[Double](0, intBarTgt), "target rt")

    // create images -- preferably pdf (iText or PdfBox)
    val png = new File(s"${dest}/${tgtId}.png")
    try {
      ChartUtils.saveChartAsPNG(png, createLineChart(dataset, tgtId), 1024, 768)
      println(s"${png.getAbsolutePath} saved")
    } catch {
      case ex: IOException =>
        println(s"Error saving chart ${png.getAbsolutePath}: ${ex.getMessage}")
    }
  }
}
