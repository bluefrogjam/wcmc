package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Ion, IonMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import org.scalatest.WordSpec

/**
  * Created by wohlg on 7/14/2016.
  */
class IncludeByRetentionIndexTimeWindowTest extends WordSpec {

  "IncludeByRetentionIndexTimeWindowTest" should {

    val filter = new IncludeByRetentionIndexTimeWindow(100,5)
    "include" in {

      assert(filter.include(new MSSpectra with CorrectedSpectra{
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val scanNumber: Int = 0
        override val ions: Seq[Ion] = List()
        override val modelIons: Option[Seq[Double]] = None
        override val msLevel: Short = 0
        override val retentionTimeInSeconds: Double = 0
        override val retentionIndex: Double = 96
        /**
          * accurate mass of this feature, if applicable
          */
        override val accurateMass: Option[Ion] = None
      }))
    }

    "include2" in {

      assert(filter.include(new MSSpectra with CorrectedSpectra{
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val scanNumber: Int = 0
        override val ions: Seq[Ion] = List()
        override val modelIons: Option[Seq[Double]] = None
        override val msLevel: Short = 0
        override val retentionTimeInSeconds: Double = 0
        override val retentionIndex: Double = 104
        /**
          * accurate mass of this feature, if applicable
          */
        override val accurateMass: Option[Ion] = None
      }))
    }

    "not included" in {

      assert(!filter.include(new MSSpectra with CorrectedSpectra{
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val scanNumber: Int = 0
        override val ions: Seq[Ion] = List()
        override val modelIons: Option[Seq[Double]] = None
        override val msLevel: Short = 0
        override val retentionTimeInSeconds: Double = 0
        override val retentionIndex: Double = 106
        /**
          * accurate mass of this feature, if applicable
          */
        override val accurateMass: Option[Ion] = None
      }))
    }

  }
}
