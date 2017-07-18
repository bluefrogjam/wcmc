package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{Reader, Writer}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.exception.RetentionIndexCorrectionException
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantificationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{LCMSProperties, Workflow, WorkflowProperties}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}

import scala.collection.JavaConverters._

/**
  * a postive mode based LCMS target workflow
  */
class LCMSPositiveModeTargetWorkflow[T] @Autowired()(properties: WorkflowProperties, writer: Writer[Sample], reader: Reader[Experiment]) extends Workflow[T](properties, writer, reader) {

  @Autowired
  val lcmsLCMSProperties: LCMSProperties = null

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  @Qualifier("quantification")
  val quantificationProcess: QuantificationProcess[T] = null

  @Autowired(required = false)
  val preProcessor: java.util.List[PreProcessor] = new util.ArrayList[PreProcessor]()

  @Autowired
  val annotate: LCMSTargetAnnotationProcess = null

  override protected def quantifySample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): QuantifiedSample[T] = sample match {
    case s: AnnotatedSample =>
      logger.info(s"quantify sample: $s")
      var temp = quantificationProcess.process(s)

      logger.info(s"running applicable postprocessing for chosen datatype: $s")
      quantificationProcess.postprocessingInstructions.asScala.foreach { x =>
        temp = x.process(temp)
      }

      temp
  }


  /**
    * this method is used to handle failed corrections
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @param exception
    * @return
    */
  override protected def handleFailedCorrection(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment, exception: Exception): Option[CorrectedSample] = {
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
              val correctionCurve = correction.process(sampleToUseForCorrection)

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
  }

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  override protected def preProcessSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): Sample = {
    if (preProcessor.isEmpty) {
      sample
    }
    else {
      //TODO could be done more elegant with a fold, but no time to play with it
      val iterator = preProcessor.asScala.sortBy(_.priortiy).reverseIterator
      var temp = iterator.next().process(sample)

      while (iterator.hasNext) {
        temp = iterator.next().process(temp)
      }

      temp
    }
  }

  /**
    * corrects the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  override protected def correctSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): CorrectedSample = correction.process(sample)

  /**
    * annotate the given sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  override protected def annotateSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): AnnotatedSample = sample match {
    case c: CorrectedSample => annotate.process(c)
  }

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @param experimentClass
    * @param experiment
    * @return
    */
  override protected def postProcessSample(sample: Sample, experimentClass: ExperimentClass, experiment: Experiment): AnnotatedSample = sample match {
    //TODO do nothing for now
    case s: AnnotatedSample => s
  }
}
