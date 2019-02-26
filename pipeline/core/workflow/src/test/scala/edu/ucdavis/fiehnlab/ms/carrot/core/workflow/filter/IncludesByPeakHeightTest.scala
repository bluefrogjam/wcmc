package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class IncludesByPeakHeightTest extends WordSpec {

  "IncludeByPeakHeightTest" should {

    "support accurate mass " must {
      val filter = new IncludesByPeakHeight(100 :: List(), massAccuracy = 0.005, minIntensity = 40.0f)

      "spectra is included since it had an Ion of I00 with Int > 30" in {
        assert(filter.include(testSpectraWith1Ion,null))
      }

      "spectra is also included since it had an Ion of I00 with Int > 30" in {
        assert(filter.include(testSpectraWith4Ions,null))
      }

      "spectra is excluded since it had an Ion of I00 with Int < 30" in {
        assert(!filter.include(testSpectraWith3Ions,null))
      }
    }

    "support nominal mass " must {
      val filter = new IncludesByPeakHeight(100 :: List(), 0.0, 40.0f)

      "spectra is included since it had an Ion of I00 with Int > 30" in {
        assert(filter.include(testSpectraWith1Ion,null))
      }

      "spectra is also included since it had an Ion of I00 with Int > 30" in {
        assert(filter.include(testSpectraWith4Ions,null))
      }

      "spectra is excluded since it had an Ion of I00 with Int < 30" in {
        assert(!filter.include(testSpectraWith3Ions,null))
      }
    }
  }
}
