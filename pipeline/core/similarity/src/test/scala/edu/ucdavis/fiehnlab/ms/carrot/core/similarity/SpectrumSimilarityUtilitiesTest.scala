package edu.ucdavis.fiehnlab.ms.carrot.core.similarity

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion

class SpectrumSimilarityUtilitiesTest extends SimilarityTest {

  "SpectrumSimilarityUtilitiesTest" should {

    "round masses using 80/20 rule" in {
      assert(SpectrumSimilarityUtilities.roundMZ(0.1) == 0)
      assert(SpectrumSimilarityUtilities.roundMZ(0.2) == 0)
      assert(SpectrumSimilarityUtilities.roundMZ(0.8) == 1)
      assert(SpectrumSimilarityUtilities.roundMZ(0.9) == 1)
      assert(SpectrumSimilarityUtilities.roundMZ(1.0) == 1)
    }

    "convert features to nominal mass spectra" in {
      val feature = buildTestFeature(Seq(Ion(1.5, 100), Ion(3.0, 10)))
      val nominalMassFeature = SpectrumSimilarityUtilities.convertToNominal(feature)

      assert(nominalMassFeature.size == 2)
      assert(nominalMassFeature.map(_._2.mass).toSet == Set(2, 3))
    }

    "convert features to nominal mass spectra and combine ions by mass" in {
      val feature = buildTestFeature(Seq(Ion(1.5, 100), Ion(2.0, 10)))
      val nominalMassFeature = SpectrumSimilarityUtilities.convertToNominal(feature)

      assert(nominalMassFeature.size == 1)
      assert(nominalMassFeature.head._2.mass == 2)
      assert(nominalMassFeature.head._2.intensity.toInt == 110)
    }
  }
}
