package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.{Similarity, SimilarityTest}

class PresenceSimilarityTest extends SimilarityTest {

  "PresenceSimilarityTest" should {

    val similarity: Similarity = new PresenceSimilarity()

    "calculate presence similarity" in {
      val a = buildTestFeature(Seq(Ion(10, 100)))
      val b = buildTestFeature(Seq(Ion(10.5, 100)))

      assert(similarity.compare(a, b, 0.51) >= 0.999)
      assert(similarity.compare(a, b, 0.49) == 0)
    }

    "handle ion presence correctly" in {
      val a = buildTestFeature(Seq(Ion(10, 100), Ion(20, 50)))
      val b = buildTestFeature(Seq(Ion(10, 100)))

      assert(similarity.compare(a, a, 1) >= 0.999)
      assert(similarity.compare(b, b, 1) >= 0.999)
      assert(similarity.compare(a, b, 1) >= 0.999)
      assert(similarity.compare(b, a, 1) < 1)
    }
  }
}
