package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode}
import org.scalatest.WordSpec

/**
  * Created by wohlg on 7/14/2016.
  */
class IncludeByRetentionIndexTimeWindowTest extends WordSpec {

  "IncludeByRetentionIndexTimeWindowTest" should {

    val filter = new IncludeByRetentionIndexWindow(100,"test",5)
    "include" in {

      assert(filter.include(new  CorrectedSpectra{
        override val retentionIndex: Double = 96

      },null))
    }

    "include2" in {

      assert(filter.include(new CorrectedSpectra{
        override val retentionIndex: Double = 104

      },null))
    }

    "not included" in {

      assert(!filter.include(new CorrectedSpectra{
        override val retentionIndex: Double = 106
      },null))
    }

  }
}
