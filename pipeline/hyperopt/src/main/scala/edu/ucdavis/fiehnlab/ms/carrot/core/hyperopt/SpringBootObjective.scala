package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.springframework.boot.{Banner, SpringApplication, WebApplicationType}
import org.springframework.context.{ApplicationContext, ConfigurableApplicationContext}
import org.apache.logging.log4j.scala.Logging

abstract class SpringBootObjective(config: Class[_], profiles: Array[String]) extends Objective[Point, Double] with Logging {

  /**
    * builds a spring boot context for us and forwards the actual stuff todo
    * to an abstract method. Spark is sooooo much fun...
    *
    * @param point
    * @return
    */
  override def apply(point: Point): Double = {
    val context: ConfigurableApplicationContext = build_context
    try {
      apply(context, point)
    }
    finally {
      context.close()
    }
  }

  private def build_context = {
    val app = new SpringApplication(config)
    app.setWebApplicationType(WebApplicationType.NONE)
    app.setBannerMode(Banner.Mode.OFF)

    //we never want to hit stasis
    val active_profiles = "carrot.nostasis" +: profiles
    app.setAdditionalProfiles(active_profiles: _*)


    val context = app.run()

    context
  }

  /**
    * actual apply function, providing subclasses with a correctly configured configuration class
    *
    * @param context
    * @param point
    * @return
    */
  def apply(context: ApplicationContext, point: Point): Double

  /**
    * allos you to warm up caches, etc before starting the spark context
    */
  def warmCaches(): Unit = {
    val begin = System.currentTimeMillis()
    logger.info("warming caches....")
    val context = build_context
    try {

      warmCaches(context)
    }
    finally {
      logger.info(s"warmup took ${(System.currentTimeMillis() - begin) / 1000}s")
      context.close()
    }
  }

  protected def warmCaches(applicationContext: ApplicationContext): Unit = {

  }

  /**
    * generates the space to be used based on the given configuration
    *
    * @param config
    * @return
    */
  def getSpace(config: Config): Map[String, Iterable[Any]]
}

/**
  * provides lcms specific helper methods to avoid code duplication
  *
  * @param config
  * @param profiles
  */
abstract class LCMSObjective(config: Class[_], profiles: Array[String]) extends SpringBootObjective(config, profiles) {


  /**
    * correctly configures our correction
    *
    * @param point
    * @param correction
    */
  def applyCorrectionSettings(point: Point, correction: LCMSTargetRetentionIndexCorrectionProcess): Unit = {
    correction.massAccuracySetting = point.get("massAccuracySetting").asInstanceOf[Double]
    correction.rtAccuracySetting = point.get("rtAccuracySetting").asInstanceOf[Double]
    correction.minPeakIntensity = point.get("minPeakIntensitySetting").asInstanceOf[Float]
    correction.intensityPenaltyThreshold = point.get("intensityPenaltyThresholdSetting").asInstanceOf[Float]
    correction.massAccuracyPPMSetting = point.get("massAccuracyPPMSetting").asInstanceOf[Double]
  }
}

/**
  * small statistics helper
  */
object Statistics {

  import Numeric.Implicits._

  def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size

  def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)

    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / (xs.size - 1)
  }

  def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(variance(xs))

  def rsdDev[T: Numeric](xs: Iterable[T]): Double = stdDev(xs) * 100 / mean(xs)

  /**
    * recursively removes outliers from a dataset outside of stdThreshold * stdDev from the mean
    *
    * @param data
    * @param sigmaThreshold cutoff distance from mean in units of standard deviation
    * @tparam T
    */
  def eliminateOutliers[T: Numeric](data: Iterable[T], stdThreshold: Double = 3): Iterable[T] = {
    val mean = Statistics.mean(data)
    val stdDev = Statistics.stdDev(data)

    val filteredData = data.filter(x => math.abs(x.toDouble - mean) <= stdThreshold * stdDev)

    if (data.size == filteredData.size) {
      data
    } else {
      eliminateOutliers(filteredData, stdThreshold)
    }
  }
}