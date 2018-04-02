package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event._
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Implementations of this class, will provide us with detailed workflows how to process and annotate data, depending on platform, etc
  */
trait Workflow[T] extends LazyLogging {

  @Autowired(required = false)
  val postActions: java.util.Collection[PostAction] = new util.ArrayList()

  @Autowired(required = false)
  val eventListeners: java.util.List[WorkflowEventListener] = List[WorkflowEventListener]().asJava

  /**
    * executes required pre processing steps, if applicable
    */
  protected final def preprocessing(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PreProcessingBeginEvent(sample)))
    val result = preProcessSample(sample, acquisitionMethod)

    eventListeners.asScala.foreach(eventListener => eventListener.handle(PreProcessingFinishedEvent(result)))
    result
  }

  protected final def postProcessing(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(PostProcessingBeginEvent(sample)))
    val result = postProcessSample(sample, acquisitionMethod)

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
      case e: Exception => handleFailedCorrection(sample, acquisitionMethod: AcquisitionMethod, e).getOrElse(sample)
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

  protected def quantifySample(sample: Sample, acquisitionMethod: AcquisitionMethod): QuantifiedSample[T]

  /**
    * annotate the given sample
    *
    * @param sample
    * @return
    */
  protected def annotateSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample

  /**
    * corrects the given sample
    *
    * @param sample
    * @return
    */
  protected def correctSample(sample: Sample, acquisitionMethod: AcquisitionMethod): CorrectedSample

  /**
    * this method is used to handle failed corrections
    *
    * @param sample
    * @param exception
    * @return
    */
  protected def handleFailedCorrection(sample: Sample, acquisitionMethod: AcquisitionMethod, exception: Exception): Option[CorrectedSample] = {
    logger.warn(s"correction failed, sample ${sample} will be removed from sample!")
    None
  }

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @return
    */
  protected def preProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @return
    */
  protected def postProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample

  /**
    * processes a sample and returns it, ready to be exported to a destination of your desire
    *
    * @param sample
    * @return
    */
  final def process(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle(ProcessBeginEvent(sample)))

    val result = postProcessing(
      quantify(
        annotation(
          correction(
            preprocessing(
              sample, acquisitionMethod
            ), acquisitionMethod
          ), acquisitionMethod
        ), acquisitionMethod
      ), acquisitionMethod
    )

    eventListeners.asScala.foreach(eventListener => eventListener.handle(ProcessFinishedEvent(sample)))
    result
  }
}
