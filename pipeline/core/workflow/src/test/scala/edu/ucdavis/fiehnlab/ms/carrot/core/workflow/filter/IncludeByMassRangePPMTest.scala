package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import org.scalatest.WordSpec

/**
  * Created by wohlg on 7/14/2016.
  */
class IncludeByMassRangePPMTest extends WordSpec {

  "IncludeByMassRangePPMTest" should {

    "include this spectra" in{
      val filter = new IncludeByMassRangePPM(new Target {
        override val precursorMass: Option[Double] = Some(100.3242)
        override var name: Option[String] = None
        override var inchiKey: Option[String] = None
        override val retentionIndex: Double = 0
        /**
          * is this a confirmed target
          */
        override var confirmed: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override var requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override var isRetentionIndexStandard: Boolean = false
        /**
          * associated spectrum propties if applicable
          */
        override val spectrum: Option[SpectrumProperties] = None
      },5)
      assert(filter.include(testAccurateMassSpectraWith4Ions2))
    }

    "but not this spectra" in {
      val filter = new IncludeByMassRangePPM(new Target {
        override val precursorMass: Option[Double] = Some(100.3249)
        override var name: Option[String] = None
        override var inchiKey: Option[String] = None
        override val retentionIndex: Double = 0
        /**
          * is this a confirmed target
          */
        override var confirmed: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override var requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override var isRetentionIndexStandard: Boolean = false

        override val spectrum: Option[SpectrumProperties] = None
      },5)
      assert(!filter.include(testAccurateMassSpectraWith4Ions2))
    }

  }
}
