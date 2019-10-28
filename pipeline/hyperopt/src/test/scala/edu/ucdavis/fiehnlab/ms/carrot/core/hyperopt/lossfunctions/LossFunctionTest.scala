package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.WordSpec

abstract class LossFunctionTest extends WordSpec {

  val CSH_SAMPLES: List[String] = List(
    "Biorec001_posCSH_preFlenniken001.mzml",
    "Biorec003_posCSH_postFlenniken020.mzml",
    "Biorec004_posCSH_postFlenniken030.mzml"
  )

  val CSH_METHOD: String = AcquisitionMethod.serialize(AcquisitionMethod(ChromatographicMethod("csh", Some("6530"), Some("test"), Some(PositiveMode()))))
}

abstract class CorrectionLossFunctionTest extends LossFunctionTest {

  val CSH_CORRECTION_TIGHT_PARAMS = Map(
    "massAccuracySetting" -> 0.01,
    "massAccuracyPPMSetting" -> 2,
    "rtAccuracySetting" -> 2,
    "minPeakIntensitySetting" -> 5000,
    "intensityPenaltyThresholdSetting" -> 50000
  )

  val CSH_CORRECTION_MODERATE_PARAMS = Map(
    "massAccuracySetting" -> 0.05,
    "massAccuracyPPMSetting" -> 5,
    "rtAccuracySetting" -> 5,
    "minPeakIntensitySetting" -> 5000,
    "intensityPenaltyThresholdSetting" -> 50000
  )

  val CSH_CORRECTION_WIDE_PARAMS = Map(
    "massAccuracySetting" -> 0.1,
    "massAccuracyPPMSetting" -> 10,
    "rtAccuracySetting" -> 10,
    "minPeakIntensitySetting" -> 1000,
    "intensityPenaltyThresholdSetting" -> 10000
  )
}