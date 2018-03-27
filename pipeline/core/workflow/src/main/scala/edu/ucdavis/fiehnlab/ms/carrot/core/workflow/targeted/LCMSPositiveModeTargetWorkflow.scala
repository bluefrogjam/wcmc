package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantificationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * a postive mode based LCMS target workflow
  */
class LCMSPositiveModeTargetWorkflow[T] @Autowired() extends Workflow[T] {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val quantificationProcess: QuantificationProcess[T] = null

  @Autowired(required = false)
  val preProcessor: java.util.List[PreProcessor] = new util.ArrayList[PreProcessor]()


  @Autowired(required = false)
  val postProcessor: java.util.List[PostProcessing[T]] = new util.ArrayList[PostProcessing[T]]()

  @Autowired
  val annotate: LCMSTargetAnnotationProcess = null

  override protected def quantifySample(sample: Sample, acquisitionMethod: AcquisitionMethod): QuantifiedSample[T] = sample match {
    case s: AnnotatedSample =>
      logger.info(s"quantify sample: $s")
      var temp = quantificationProcess.process(s, acquisitionMethod)

      logger.info(s"running ${quantificationProcess.postprocessingInstructions.size()} applicable postprocessing for chosen data type: $s")
      quantificationProcess.postprocessingInstructions.asScala.foreach { x =>
        logger.info(s"executing: $x")
        temp = x.process(temp, acquisitionMethod)
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
  override protected def handleFailedCorrection(sample: Sample, acquisitionMethod: AcquisitionMethod, exception: Exception): Option[CorrectedSample] = {
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
    logger.warn("TODO!!!")
    None
  }

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @return
    */
  override protected def preProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = {
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
  override protected def correctSample(sample: Sample, acquisitionMethod: AcquisitionMethod): CorrectedSample = correction.process(sample, acquisitionMethod)

  /**
    * annotate the given sample
    *
    * @param sample
    * @return
    */
  override protected def annotateSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample = sample match {
    case c: CorrectedSample => annotate.process(c, acquisitionMethod)
  }

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @return
    */
  override protected def postProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample = sample match {
    case s: QuantifiedSample[T] =>
      if (postProcessor.isEmpty) {
        s
      }
      else {
        //TODO could be done more elegant with a fold, but no time to play with it
        val iterator = postProcessor.asScala.sortBy(_.priortiy).reverseIterator
        var temp = iterator.next().process(s, acquisitionMethod)

        while (iterator.hasNext) {
          temp = iterator.next().process(temp, acquisitionMethod)
        }

        temp
      }

  }
}
