package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import org.scalatest.{FunSuite, WordSpec}
import edu.ucdavis.fiehnlab.ms.carrot.core.api._

/**
  * Created by wohlgemuth on 6/26/16.
  */
class IncludeByRetentionTimeWindowTest extends WordSpec {

  "the filter" should {


    "include this spectra" in{
      val filter = new IncludeByRetentionTimeWindow(996,5)

      assert(filter.include(testSpectraWith1Ion))

    }

    "but not this spectra" in {
      val filter = new IncludeByRetentionTimeWindow(991,5)

      assert(!filter.include(testSpectraWith1Ion))

    }
  }

}
