package edu.ucdavis.fiehnlab.utilities.minix

import java.io.File
import java.util

import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager

@SpringBootTest
class SXStudyFileReaderTest extends WordSpec with ShouldMatchers {

  @Autowired
  val sXStudyFilesTransformer:SXStudyFileReader = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "in the first file" must {
    val result = sXStudyFilesTransformer.loadData(new File(getClass.getResource("/SXStudyFile.xml").toURI()))

    "there should be 6 samples" in {
      result.size() should be(6)
    }

    "there should be 410 annotations in the first sample" in {
      result.get(0).getAnnotations().size() should be(410)
    }

    "there should be 394 annotations in the second sample" in {
      result.get(1).getAnnotations().size() should be(394)
    }
  }

  "in the second file" must {
    val result = sXStudyFilesTransformer.loadData(new File(getClass.getResource("/SXStudyFile - 2.xml").toURI()))

    "there should be 2 samples" in {
      result.size() should be(2)
    }

    "there should be 3 annotations in the first sample" in {
      result.get(0).getAnnotations().size() should be(3)
    }

    "there should be 2 annotations in the second sample" in {
      result.get(1).getAnnotations().size() should be(2)
    }

    "the first purity in the first sample should be 0.50386" in {
      val annotations = new util.ArrayList(result.get(0).getAnnotations())

      annotations.get(0).getPurity() should be(0.50386)
    }

  }


}

@SpringBootApplication
class SXStudyFileTestConfiguration{

}