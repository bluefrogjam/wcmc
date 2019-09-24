package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.rules

import com.eharmony.spotz.Preamble.Point
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, Sample}

/**
  * basic rule for a successful evaluation
  */
abstract class EvaluationRule[T <: Sample] extends Serializable {

  /**
    * do we accept this result
    *
    * @param sample
    * @return
    */
  def accept(sample: T): Boolean


}

/**
  * we require a minimum of annotations to be considered successful
  *
  * @param minimumCount
  * @tparam T
  */
class AnnotationCountEvaluationRule[T <: AnnotatedSample](val minimumCount: Int) extends EvaluationRule[T] {
  /**
    * do we accept this result
    *
    * @param sample
    * @return
    */
  override def accept(sample: T): Boolean = sample.spectra.size > minimumCount
}
class RuleViolatedException[T <: Sample](sample: T, rule: EvaluationRule[T], point: Point) extends Exception(s"${sample} violiated ${rule} for point ${point}")