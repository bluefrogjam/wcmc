package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import org.scalatest.WordSpec
import edu.ucdavis.fiehnlab.ms.carrot.core.api._

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class IncludesBasePeakSpectraTest extends WordSpec {

  "IncludeBasePeakSpectraTest" should {

    val filter = new IncludesBasePeakSpectra(List(100))

    "include spectra with basepeak 100" in {

      assert(filter.include(testSpectraWith1Ion))
    }
    "include another spectra with basepeak 100" in {

      assert(filter.include(testSpectraWith2Ions))
    }

    "do not include spectra with basepeak 130" in {

      assert(!filter.include(testSpectraWith3Ions))
    }


  }
}
