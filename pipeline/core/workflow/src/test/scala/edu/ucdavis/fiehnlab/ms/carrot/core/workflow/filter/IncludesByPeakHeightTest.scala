package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class IncludesByPeakHeightTest extends WordSpec {

  "IncludeByPeakHeightTest" should {

    "support accurate mass " must {
      val filter = new IncludesByPeakHeight(100 :: List(),"test")

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
      val filter = new IncludesByPeakHeight(100 :: List(),"test",0.0,0.0f)

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
