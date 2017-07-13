package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._

import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class IncludesMSLevelSpectraTest extends WordSpec {

  "IncludeMSLevelSpectraTest" should {

    "support MS 1 level " must {
      val filter = new IncludesMSLevelSpectra(1)

      "include MS 1 spectra" in {
        assert(filter.include(testSpectraWith1Ion))
      }

    }

    "support MS 2 level" must {
      val filter = new IncludesMSLevelSpectra(2)

      "not include MS 1 spectra" in {
        assert(!filter.include(testSpectraWith1Ion))
      }

    }



  }
}
