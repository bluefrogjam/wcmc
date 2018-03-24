package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow

/**
  * this is the revised binbase data processing workflow
  * @tparam Double
  */
class BinBaseWorkflow[Double] extends Workflow[Double]{

  override protected def quantifySample(sample: Sample, acquisitionMethod: AcquisitionMethod): QuantifiedSample[Double] = ???

  /**
    * annotate the given sample
    *
    * @param sample
    * @return
    */
  override protected def annotateSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample = ???

  /**
    * corrects the given sample
    *
    * @param sample
    * @return
    */
  override protected def correctSample(sample: Sample, acquisitionMethod: AcquisitionMethod): CorrectedSample = ???

  /**
    * preprocesses the given sample
    *
    * @param sample
    * @return
    */
  override protected def preProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): Sample = ???

  /**
    * provides us with a post processed sample
    *
    * @param sample
    * @return
    */
  override protected def postProcessSample(sample: Sample, acquisitionMethod: AcquisitionMethod): AnnotatedSample = ???
}
