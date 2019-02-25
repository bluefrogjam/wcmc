package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.{testSpectraWith1Ion, testSpectraWith3Ions, testSpectraWith4Ions}
import org.scalatest.WordSpec

class IncludeByIonRatioTest extends WordSpec {

  "IncludeByIonRatioTest" should {

    "support accurate mass " must {
      val filter = new IncludeByIonRatio(100.0, 0.45, 0.55, 0.005)

      "spectra not included since it has no 140" in {
        assert(!filter.include(testSpectraWith1Ion,null))
      }

      "spectra included since since it has 100 and ratio is high enough at 0.5" in {
        assert(filter.include(testSpectraWith4Ions,null))
      }

      "spectra is excluded since it had an Ion of I00, ratio is to low" in {
        assert(!filter.include(testSpectraWith3Ions,null))
      }
    }
  }
}
