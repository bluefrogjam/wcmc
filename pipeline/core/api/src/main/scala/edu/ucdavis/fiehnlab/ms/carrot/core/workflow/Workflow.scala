package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{Reader, Writer}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event._
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Implementations of this class, will provide us with detailed workflows how to process and annotate data, depending on platform, etc
  */
abstract class Workflow[T](val properties: WorkflowProperties, writer: Writer[Sample], reader: Reader[Experiment], val eventListeners: java.util.List[WorkflowEventListener] = List().asJava) extends ItemProcessor[InputStream, InputStream] with LazyLogging {

  /**
    * executes required pre processing steps, if applicable
    */
  protected def preprocessing(experiment: Experiment): Experiment = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (PreProcessingBeginEvent(experiment)))
    val result = assembleExperiment(
      experiment, preProcessSample
    )
    eventListeners.asScala.foreach(eventListener => eventListener.handle (PreProcessingFinishedEvent(result)))
    result
  }

  /**
    * executes the retention index correction, if applicable
    */
  protected def correction(experiment: Experiment): Experiment = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (CorrectionBeginEvent(experiment)))

    val result =
      assembleExperiment(
        experiment, correctSample, handleFailedCorrection
      )

    eventListeners.asScala.foreach(eventListener => eventListener.handle (CorrectionFinishedEvent(result)))
    result
  }

  /**
    * executes the annotation, if applicable
    */
  protected def annotation(experiment: Experiment): Experiment = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (AnnotationBeginEvent(experiment)))
    val result = assembleExperiment(
      experiment, annotateSample
    )
    eventListeners.asScala.foreach(eventListener => eventListener.handle (AnnotationFinishedEvent(result)))
    result
  }

  /**
    * quantify the sample
    *
    * @param experiment
    * @return
    */
  protected def quantify(experiment: Experiment): Experiment = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (QuantificationBeginEvent(experiment)))
    val result = assembleExperiment(
      experiment, quantifySample
    )
    eventListeners.asScala.foreach(eventListener => eventListener.handle (QuantificationFinishedEvent(result)))
    result
  }

  protected def quantifySample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): QuantifiedSample[T]

  /**
    * annotate the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  protected def annotateSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): AnnotatedSample

  /**
    * corrects the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  protected def correctSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): CorrectedSample

  /**
    * this method is used to handle failed corrections
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @param exception
    * @return
    */
  protected def handleFailedCorrection(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment, exception: Exception): Option[CorrectedSample] = None

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  protected def preProcessSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Sample

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  protected def postProcessSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Sample

  /**
    * assembles a new experiment, based on the provided callback function
    *
    * @param experiment        experiment
    * @param callback          method to handle the processing
    * @param exceptionCallBack what todo in case of an exception and is an optional argument
    * @return
    */
  private def assembleExperiment(experiment: Experiment, callback: (Sample, ExperimentClass, Experiment) => Sample, exceptionCallBack: (Sample, ExperimentClass, Experiment, Exception) => Option[Sample] = null) = {
    Experiment(
      classes = experiment.classes.collect {
        case clazz: ExperimentClass =>
          ExperimentClass(
            samples = clazz.samples.collect {
              case sample: Sample =>
                try {
                  callback(sample, clazz, experiment)
                } catch {
                  case e: Exception =>

                    val exceptionHandling = if (exceptionCallBack != null) {
                      logger.debug(s"utilizing providing exception handling to handle: ${e.getMessage}")
                      exceptionCallBack(sample, clazz, experiment, e)
                    }
                    else {
                      logger.warn(s"error in sample found, skipping it: ${sample.fileName}")
                      logger.warn(e.getMessage, e)

                      None
                    }

                    if (exceptionHandling.isDefined) {
                      exceptionHandling.get
                    }
                    else {
                      None
                    }
                }
            }.collect {
              case sample: Sample => sample
            },

            name = clazz.name,
            organ = clazz.organ,
            species = clazz.species,
            treatments = clazz.treatments
          )
      },
      name = experiment.name
    )
  }

  /**
    * exports the data and provides us with an input stream, where we can read the result from
    */
  protected def export(experiment: Experiment): InputStream = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (ExportBeginEvent(experiment)))

    val file = File.createTempFile("carrot-target", ".txt")
    val out = new BufferedOutputStream(new FileOutputStream(file))

    logger.info(s"\ttemp file is ${file.getAbsolutePath}")
    val classes: Seq[ExperimentClass] = experiment.classes

    writer.writeHeader(out)
    classes.foreach { clazz =>
      clazz.samples.foreach { sample =>
        logger.info(s"writing sample ${sample}")
        writer.write(out, sample)
        out.flush()
      }
    }
    writer.writeFooter(out)
    out.flush()
    out.close()

    eventListeners.asScala.foreach(eventListener => eventListener.handle (ExportFinishedEvent(experiment)))
    new FileInputStream(file)
  }

  /**
    * runs the whole workflow for an experiment
    */
  final def process(inputStream: InputStream): InputStream = {

    assert(inputStream != null)
    assert(reader != null)

    val experiment: Experiment = reader.read(inputStream)

    process(experiment)
  }

  /**
    * alternate method, to go directly from an experiment and should only used in tests, etc
    * @param experiment
    * @return
    */
  final def process(experiment:Experiment) : InputStream = {
    eventListeners.asScala.foreach(eventListener => eventListener.handle (ProcessBeginEvent(experiment)))

    val result = export(
      quantify(
        annotation(
          correction(
            preprocessing(
              experiment
            )
          )
        )
      )
    )

    eventListeners.asScala.foreach(eventListener => eventListener.handle (ProcessFinishedEvent(experiment)))

    result
  }
}