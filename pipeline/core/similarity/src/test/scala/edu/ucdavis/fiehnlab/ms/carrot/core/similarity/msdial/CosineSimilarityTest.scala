package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.{Similarity, SimilarityTest}

class CosineSimilarityTest extends SimilarityTest {

  "SpectrumSimilarityUtilitiesTest" should {

    val similarity: Similarity = new CosineSimilarity(false)

    "calculate dot product similarity" in {
      val a = buildTestFeature(Seq(Ion(10, 100)))
      val b = buildTestFeature(Seq(Ion(10.5, 100)))

      assert(similarity.compare(a, b, 0.51) >= 0.999)
      assert(similarity.compare(a, b, 0.49) == 0)
    }

    "apply peak penalty" in {
      val a = buildTestFeature(Seq(Ion(10, 100)))
      assert(new CosineSimilarity().compare(a, a, 1) < similarity.compare(a, a, 1))
    }
  }
}
