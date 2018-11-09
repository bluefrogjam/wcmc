package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event._
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Implementations of this class, will provide us with detailed workflows how to process and annotate data, depending on platform, etc
  */
class Workflow[T] extends LazyLogging {

  @Autowired(required = false)
  val postActions: java.util.Collection[PostAction] = new util.ArrayList()

  @Autowired(required = false)
  val eventListeners: java.util.List[WorkflowEventListener] = List[WorkflowEventListener]().asJava

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val quantificationProcess: QuantificationProcess[T] = null

  @Autowired(required = false)
  val preProcessor: java.util.List[PreProcessor] = new util.ArrayList[PreProcessor]()


  @Autowired(required = false)
  val postProcessor: java.util.List[PostProcessing[T]] = new util.ArrayList[PostProcessing[T]]()

  @Autowired
  val annotate: AnnotateSampleProcess = null

  @Autowired
  val stasisClient: StasisService = null

  /**
    * executes required pre processing steps, if applicable
    */
  protected final def preprocessing(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PreProcessingBeginEvent(sample)))
    val result = preProcessSample(sample, acquisitionMethod)
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PreProcessingFinishedEvent(result)))
    result
  }

  protected final def postProcessing(sample: Sample, acquisitionMethod: AcquisitionMethod, rawSample: Option[Sample]): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PostProcessingBeginEvent(sample)))
    val result = postProcessSample(sample, acquisitionMethod, rawSample)
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PostProcessingFinishedEvent(result)))
    result
  }

  /**
    * executes the retention index correction, if applicable
    */
  protected final def correction(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(CorrectionBeginEvent(sample)))

    val result: Sample = try {
      correctSample(sample, acquisitionMethod)
    }
    catch {
      case e: Exception =>
        handleFailedCorrection(sample, acquisitionMethod: AcquisitionMethod, e).getOrElse(sample)
    }

    eventListeners.asScala.foreach(eventListener => eventListener.handle(CorrectionFinishedEvent(result)))
    result
  }

  /**
    * executes the annotation, if applicable
    */
  protected final def annotation(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(AnnotationBeginEvent(sample)))
    val result = annotateSample(sample, acquisitionMethod)
    eventListeners.asScala.foreach(eventListener => eventListener.handle(AnnotationFinishedEvent(result)))
    result
  }

  /**
    * quantify the sample
    *
    * @param sample
    * @return
    */
  protected final def quantify(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(QuantificationBeginEvent(sample)))
    val result = quantifySample(sample, acquisitionMethod)
    eventListeners.asScala.foreach(eventListener => eventListener.handle(QuantificationFinishedEvent(result)))
    result
  }

  /**
    * processes a sample and returns it, ready to be exported to a destination of your desire
    *
    * @param sample
    * @return
    */
  final def process(sample: Sample, acquisitionMethod: AcquisitionMethod, rawSample: Option[Sample] = None): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(ProcessBeginEvent(sample)))

    val quantified = quantify(
      annotation(
        correction(
          preprocessing(
            sample, acquisitionMethod
          ), acquisitionMethod
        ), acquisitionMethod
      ), acquisitionMethod
    )

    val result = postProcessing(quantified, acquisitionMethod, rawSample)

    eventListeners.asScala.foreach(eventListener => eventListener.handle(ProcessFinishedEvent(sample)))
    result
  }

  protected def quantifySample(sample: Sample, acquisitionMethod: AcquisitionMethod): QuantifiedSample[T] = sample match {
    case s: AnnotatedSample =>
      logger.info(s"quantify sample: $s")
      var temp = quantificationProcess.process(s, acquisitionMethod, None)

      logger.info(s"running ${quantificationProcess.postprocessingInstructions.size()} applicable postprocessing for chosen data type: $s")
      quantificationProcess.postprocessingInstructions.asScala.foreach { x =>
        logger.info(s"executing: $x")
        temp = x.process(temp, acquisitionMethod, None)
      }

      temp
  }


  /**
    * this method is used to handle failed corrections
    *
    * @param sample
    * @param exception
    * @return
    */
  protected def handleFailedCorrection(sample: Sample, acquisitionMethod: AcquisitionMethod, exception: Exception): Option[CorrectedSample] = {
    /*
    if (lcmsLCMSProperties.allowCorrectionFailedFallback) {
      exception match {
        case e: RetentionIndexCorrectionException =>

          /**
            * simple function doing the actual correction for us
            *
            * @return
            */
          def correct(sampleToUseForCorrection: Sample): Option[CorrectedSample] = {
            try {
              val correctionCurve = correction.process(sampleToUseForCorrection, experiment.acquisitionMethod)

              Some(correction.doCorrection(correctionCurve.featuresUsedForCorrection, sample, correctionCurve.regressionCurve, sampleToUseForCorrection))
              //correction successful
            }
            catch {
              case x: RetentionIndexCorrectionException => //correction failed, let's move on
                logger.trace(x.getMessage, x)
                None
            }
          }

          //find first sample, which has a successful correction, based on the sample list
          //ensure that we won't match the sample against it self
          val result: Option[CorrectedSample] = experimentClass.samples.filterNot(_.fileName == sample.fileName).collectFirst {
            //sadly needs to be evaluated twice, once to filter and once to actually return the calculated result
            case s if correct(s).isDefined =>
              val c = correct(s).get
              logger.debug(s"corrected ${c}")
              logger.debug(s"\t=> with ${c.correctedWith}")

              c
          }

          result
        case _ => None

      }
    }
    else {
      None
    }
    */
    logger.warn(s"Correction failed misserably with error: ${exception.getMessage}")
    stasisClient.addTracking(TrackingData(sample.name, "failed", sample.fileName))
    None
  }

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @return
    */
  protected def preProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    if (preProcessor.isEmpty) {
      logger.info(s"PreProcessors: None")
      sample
    }
    else {
      logger.info(s"PreProcessors: ${preProcessor.asScala.sortBy(_.priortiy).reverse.map(_.getClass.getSimpleName).mkString(";")}")

      //TODO could be done more elegant with a fold, but no time to play with it
      val iterator = preProcessor.asScala.sortBy(_.priortiy).reverseIterator
      var temp = iterator.next().process(sample, acquisitionMethod)

      while (iterator.hasNext) {
        temp = iterator.next().process(temp, acquisitionMethod)
      }

      temp
    }
  }

  /**
    * corrects the given sample
    *
    * @param sample
    * @return
    */
  protected def correctSample(sample: Sample, acquisitionMethod: AcquisitionMethod): CorrectedSample = correction.process(sample, acquisitionMethod)

  /**
    * annotate the given sample
    *
    * @param sample
    * @return
    */
  protected def annotateSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample = sample match {
    case c: CorrectedSample => annotate.process(c, acquisitionMethod)
  }

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @return
    */
  protected def postProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod, rawSample: Option[Sample]): AnnotatedSample = sample match {
    case s: QuantifiedSample[T] =>
      if (postProcessor.isEmpty) {
        s
      }
      else {
        //TODO could be done more elegant with a fold, but no time to play with it
        val iterator = postProcessor.asScala.sortBy(_.priortiy).reverseIterator
        var temp = iterator.next().process(s, acquisitionMethod, rawSample)

        while (iterator.hasNext) {
          temp = iterator.next().process(temp, acquisitionMethod, rawSample)
        }

        temp
      }

  }
}
