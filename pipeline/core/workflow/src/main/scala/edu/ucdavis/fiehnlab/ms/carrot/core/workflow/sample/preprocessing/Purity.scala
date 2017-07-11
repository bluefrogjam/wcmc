package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.{IncludeByMassRange, IncludeByRetentionTimeWindow, IncludeBySimilarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.Process
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{ProcessedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * computes the purity of a spectra
  */
@Component
class PurityProcessing @Autowired()(val properties: WorkflowProperties) extends Process[Sample, ProcessedSample] with LazyLogging {


  /**
    * how many seconds around the spectra should be utilized for the purity calculation
    */
  @Value("${wcms.pipeline.workflow.config.purity.retentionTimeRange:15}")
  val retentionTimeRange: Double = 15

  /**
    * mass range in dalton
    */
  @Value("${wcms.pipeline.workflow.config.purity.massRange:3.0}")
  val massRange: Double = 3

  /**
    * defines our min similarity cutoff for 2 spectra considered to be similar
    */
  @Value("${wcms.pipeline.workflow.config.purity.ms.similarity:0.5}")
  val minSimilarity: Double = 0.5


  /**
    * computes the purity for all spectra in this sample
    * and returns an instance of a processed sample
    *
    * @param input
    * @return
    */
  override def doProcess(input: Sample): ProcessedSample = {
    logger.debug("computing purity for all spectra")

    //generates a tuple of all our purity elements with an associated scan
    val purityConverted: Seq[(_ <: Feature, Double)] = input.spectra.collect {

      case ms: MSSpectra =>
        val purity = computePurity(ms, input)
        (ms, purity)
    }

    logger.debug("scaling purity for all spectra between 0 and 1")
    val minPurity: Double = purityConverted.map(_._2).min
    val maxPurity: Double = purityConverted.map(_._2).max

    //scale everything
    val scaled: Seq[_ <: Feature] = purityConverted.collect {
      case p: (Feature, Double) =>
        val purity = (p._2 - minPurity) / (maxPurity - minPurity)

        //assemble our return type
        p._1 match {
          case msms: MSMSSpectra =>
            logger.debug(s"\t=> scaled purity is $purity vs ${p._2} for scan ${msms.scanNumber}")
            MSMSSpectraImpl(
              msms.scanNumber, msms.ions, msms.retentionTimeInSeconds, msms.msLevel, msms.ionMode, msms.modelIons, Some(purity), msms.precursorIon, msms.accurateMass)

          case ms: MSSpectra =>
            logger.debug(s"\t=> scaled purity is $purity vs ${p._2} for scan ${ms.scanNumber}")
            MSSpectraImpl(ms.scanNumber, ms.ions, ms.retentionTimeInSeconds, ms.msLevel, ms.ionMode, ms.modelIons, Some(purity), ms.accurateMass)
        }
    }

    new ProcessedSample {
      override val spectra: Seq[_ <: Feature] = scaled
      override val fileName: String = input.fileName
    }

  }

  /**
    * does the acutal computation
    *
    * @param spectra
    * @param sample
    * @return
    */
  def computePurity(spectra: MSSpectra, sample: Sample): Double = {
    logger.debug("filtering surounded spectra..")
    val time = new IncludeByRetentionTimeWindow(spectra.retentionTimeInSeconds, retentionTimeRange)
    val mass = new IncludeByMassRange(spectra.basePeak.mass, massRange)
    val similiarity = new IncludeBySimilarity(spectra, minSimilarity)

    //filter by RI range
    val evaluateTime = sample.spectra.par.
      filter(_.scanNumber != spectra.scanNumber).
      filter(time.include).collect {
      case x: MSSpectra =>
        x
      case x: Feature =>
        null
    }.filter(_ != null)

    logger.debug(s"\t\t=> ${evaluateTime.size} spectra kept after retention time range filter")

    //filter by mass window
    val evaluateMass = evaluateTime.par.
      filter(mass.include)

    logger.debug(s"\t\t=> ${evaluateMass.size} spectra kept after mass range filter")

    //filter by similarity score
    val evaluateSimilarity = evaluateMass.par.
      filter(similiarity.include)

    logger.debug(s"\t\t=> ${evaluateSimilarity.size} spectra kept after similarity filter")

    val averageTic: Double = evaluateTime.map(_.tic).sum / evaluateTime.size

    logger.debug(s"\t\t=> average tic is $averageTic vs ${spectra.tic} tic of spectra")

    if (evaluateTime.isEmpty) {
      0.0
    }
    else {
      val score: Double = evaluateSimilarity.size * 0.79 + averageTic / spectra.tic + evaluateTime.size.toDouble * 0.01 + evaluateMass.size.toDouble * 0.2
      logger.debug(s"\t\t=> computed none scaled purity $score for scan ${spectra.scanNumber}")
      score
    }
  }
}
