package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class ExcludeBasePeakSpectraTest extends WordSpec {

  "ExcludeBasePeakSpectraTest" should {

    val filter = new ExcludeBasePeakSpectra(100 :: List(),"test")

    "base peak with base peak 100 should be excluded" in {
      assert(!filter.include(testSpectraWith1Ion,null))
    }

    "base peak with basepeak 130 should be included" in {
      assert(filter.include(testSpectraWith3Ions,null))
    }
  }
}
