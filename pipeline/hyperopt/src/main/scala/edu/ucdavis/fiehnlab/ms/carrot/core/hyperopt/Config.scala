package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import net.jcazevedo.moultingyaml._

object ConfigYamlProtocol extends DefaultYamlProtocol {
  implicit val correctionSettingsFormat = yamlFormat5(CorrectionSettings)
  implicit val correctionFormat = yamlFormat1(Correction)
  implicit val stagesFormat = yamlFormat1(Stages)
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

case class Correction(
                       settings: CorrectionSettings
                     )

case class Stages(
                   correction: Option[Correction]
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
