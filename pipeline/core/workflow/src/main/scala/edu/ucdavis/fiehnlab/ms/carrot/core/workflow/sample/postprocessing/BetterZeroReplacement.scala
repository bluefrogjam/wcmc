package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import org.springframework.beans.factory.annotation.Autowired

/**
	* Created by diego on 7/25/2017.
	*/
class BetterZeroReplacement @Autowired()(properties: WorkflowProperties) extends PostProcessing[Double](properties) with LazyLogging {
	logger.warn(s"Creating ${this.getClass.getSimpleName}")

	@Autowired
	val correction: LCMSTargetRetentionIndexCorrection = null

	@Autowired
	val sampleLoader: SampleLoader = null

	/**
		* actually processes the item (implementations in subclasses)
		*
		* @param sample
		* @return
		*/
	override def doProcess(sample: QuantifiedSample[Double]): QuantifiedSample[Double] = {
		sample

		/* how do i get the raw spectrum (ms1 or msms) from a quantified sample??? */

		//
		//		val rawdata: Option[Sample] = sampleLoader.loadSample(sample.fileName)
		//
		//		if (rawdata.isDefined) {
		//			logger.info(s"replacing data with: ${rawdata}")
		//			val correctedRawData: CorrectedSample = correction.doCorrection(sample.featuresUsedForCorrection, rawdata.get, sample.regressionCurve, sample)
		//
		//			logger.info(s"corrected data for: ${correctedRawData}")
		//
		//			val replacedSpectra = sample.quantifiedTargets.par.map { target =>
		//				if (target.quantifiedValue.isDefined) {
		//					target
		//				}
		//				else {
		//					try {
		//						replaceValue(target, sample, correctedRawData)
		//					} catch {
		//						case e:Exception =>
		//							logger.warn(s"replacement faild for entry, ignore for now: ${e.getMessage}",e)
		//							target
		//					}
		//				}
		//			}.seq
		//
		//			new QuantifiedSample[Double] {
		//				override val quantifiedTargets: Seq[QuantifiedTarget[Double]] = replacedSpectra
		//				override val noneAnnotated: Seq[_ <: Feature with CorrectedSpectra] = sample.noneAnnotated
		//				override val correctedWith: Sample = sample.correctedWith
		//				override val featuresUsedForCorrection: Seq[TargetAnnotation[RetentionIndexTarget, Feature]] = sample.featuresUsedForCorrection
		//				override val regressionCurve: Regression = sample.regressionCurve
		//				override val fileName: String = sample.fileName
		//			}
		//		}
		//		//toss exception
		//		else {
		//			logger.warn(s"sorry we were not able to load the rawdata file for ${sample.name} using the loader ${sampleLoader}, we are skipping this replacement")
		//			sample
		//		}
	}
}
