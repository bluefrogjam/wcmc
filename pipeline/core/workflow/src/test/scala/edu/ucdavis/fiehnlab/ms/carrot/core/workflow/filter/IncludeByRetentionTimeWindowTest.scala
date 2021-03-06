package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 6/26/16.
  */
class IncludeByRetentionTimeWindowTest extends WordSpec {

  "the filter" should {


    "include this spectra" in{
      val filter = new IncludeByRetentionTimeWindow(996, 5)

      assert(filter.include(testSpectraWith1Ion,null))

    }

    "but not this spectra" in {
      val filter = new IncludeByRetentionTimeWindow(991, 5)

      assert(!filter.include(testSpectraWith1Ion,null))

    }
  }

}
