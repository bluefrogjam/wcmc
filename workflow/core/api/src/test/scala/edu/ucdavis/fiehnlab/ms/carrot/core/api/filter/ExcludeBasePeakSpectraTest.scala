package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import org.scalatest.WordSpec
import edu.ucdavis.fiehnlab.ms.carrot.core.api._

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class ExcludeBasePeakSpectraTest extends WordSpec {

  "ExcludeBasePeakSpectraTest" should {

    val filter = new ExcludeBasePeakSpectra(100 :: List())

    "base peak with base peak 100 should be excluded" in {
      assert(!filter.include(testSpectraWith1Ion))
    }

    "base peak with basepeak 130 should be included" in {
      assert(filter.include(testSpectraWith3Ions))
    }
  }
}
