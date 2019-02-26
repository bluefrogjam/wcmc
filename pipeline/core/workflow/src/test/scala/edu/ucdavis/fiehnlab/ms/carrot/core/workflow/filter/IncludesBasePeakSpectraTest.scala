package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class IncludesBasePeakSpectraTest extends WordSpec {

  "IncludeBasePeakSpectraTest" should {

    "for accurate matches " must {
      val filter = new IncludesBasePeakSpectra(List(100))

      "include spectra with basepeak 100" in {

        assert(filter.include(testSpectraWith1Ion,null))
      }
      "include another spectra with basepeak 100" in {

        assert(filter.include(testSpectraWith2Ions,null))
      }

      "do not include spectra with basepeak 130" in {

        assert(!filter.include(testSpectraWith3Ions,null))
      }
    }

    "for nominal masses" must {
      val filter = new IncludesBasePeakSpectra(List(100), 0.0)

      "include spectra with basepeak 100" in {
        assert(filter.include(testSpectraWith1Ion,null))
      }
      "include another spectra with basepeak 100" in {

        assert(filter.include(testSpectraWith2Ions,null))
      }

      "do not include spectra with basepeak 130" in {

        assert(!filter.include(testSpectraWith3Ions,null))
      }
    }
  }
}
