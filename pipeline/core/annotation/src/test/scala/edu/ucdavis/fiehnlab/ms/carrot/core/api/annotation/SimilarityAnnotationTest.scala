package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.math.similarity.{CompositeSimilarity, CosineSimilarity}
import org.scalatest.WordSpec
import Test._

/**
  * Created by wohlg_000 on 6/13/2016.
  */
class SimilarityAnnotationTest extends WordSpec {

  "SimilarityAnnotationTest" should {

    "isMatch" in {

      val test = new SimilarityAnnotation(0.7,new CompositeSimilarity())

      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "noMatch" in {
      val test = new SimilarityAnnotation(0.9,new CompositeSimilarity())

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))

    }

  }
}
