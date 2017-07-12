package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import org.scalatest.WordSpec
import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target

/**
  * Created by wohlg on 7/14/2016.
  */
class IncludeByMassRangePPMTest extends WordSpec {

  "IncludeByMassRangePPMTest" should {

    "include this spectra" in{
      val filter = new IncludeByMassRangePPM(new Target {
        override val monoIsotopicMass: Option[Double] = Some(100.3242)
        override val name: Option[String] = None
        override val inchiKey: Option[String] = None
        override val retentionTimeInSeconds: Double = 0
      },5)
      assert(filter.include(testAccurateMassSpectraWith4Ions2))
    }

    "but not this spectra" in {
      val filter = new IncludeByMassRangePPM(new Target {
        override val monoIsotopicMass: Option[Double] = Some(100.3249)
        override val name: Option[String] = None
        override val inchiKey: Option[String] = None
        override val retentionTimeInSeconds: Double = 0
      },5)
      assert(!filter.include(testAccurateMassSpectraWith4Ions2))
    }

  }
}
