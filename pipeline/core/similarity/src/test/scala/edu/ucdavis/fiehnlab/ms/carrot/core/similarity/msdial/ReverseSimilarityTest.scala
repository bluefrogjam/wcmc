package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.{Similarity, SimilarityTest}

class ReverseSimilarityTest extends SimilarityTest {

  "ReverseSimilarityTest" should {

    val similarity: Similarity = new ReverseSimilarity(false)

    "calculate reverse similarity" in {
      val a = buildTestFeature(Seq(Ion(10, 100)))
      val b = buildTestFeature(Seq(Ion(10.5, 100)))

      assert(similarity.compare(a, b, 0.51) >= 0.999)
      assert(similarity.compare(a, b, 0.49) == 0)
    }

    "apply peak penalty" in {
      val a = buildTestFeature(Seq(Ion(10, 100)))
      assert(new ReverseSimilarity().compare(a, a, 1) < similarity.compare(a, a, 1))
    }

    "ensure that ordering results in different scores" in {
      val a = buildTestFeature(Seq(Ion(10, 100), Ion(20, 50)))
      val b = buildTestFeature(Seq(Ion(10, 100)))

      assert(similarity.compare(a, b, 1) >= similarity.compare(b, a, 1))
    }
  }
}
