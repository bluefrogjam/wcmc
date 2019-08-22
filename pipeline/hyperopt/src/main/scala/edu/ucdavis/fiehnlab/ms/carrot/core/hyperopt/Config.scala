package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import net.jcazevedo.moultingyaml._

object ConfigYamlProtocol extends DefaultYamlProtocol {
  implicit val annotationSettingsFormat = yamlFormat9(AnnotationSettings)
  implicit val correctionSettingsFormat = yamlFormat5(CorrectionSettings)
  implicit val annotationFormat = yamlFormat1(Annotation)
  implicit val correctionFormat = yamlFormat1(Correction)
  implicit val stagesFormat = yamlFormat2(Stages)
  implicit val hyperoptFormat = yamlFormat5(Hyperopt)
  implicit val configFormat = yamlFormat1(Config)
}

case class CorrectionSettings(
                               massAccuracyPPM: List[Double],
                               massAccuracy: List[Double],
                               rtAccuracy: List[Double],
                               minPeakIntensity: List[Float],
                               intensityPenalty: List[Float]
                             )

case class AnnotationSettings(
                               recursive: List[Boolean],
                               preferMassAccuracy: List[Boolean],
                               preferGaussianSimilarity: List[Boolean],

                               closePeakDetection: List[Double],
                               massAccuracyPPM: List[Double],
                               massAccuracy: List[Double],

                               rtIndexWindow: List[Double],
                               massIntensity: List[Float],
                               intensityPenalty: List[Float]
                             )

case class Correction(
                       settings: CorrectionSettings
                     )

case class Annotation(
                       settings: AnnotationSettings
                     )

case class Stages(
                   correction: Option[Correction] = None,
                   annotation: Option[Annotation] = None
                 )

case class Hyperopt(
                     spark: String,
                     samples: List[String],
                     profiles: List[String],
                     method: String,
                     stages: Stages
                   )

case class Config(
                   hyperopt: Hyperopt
                 )
