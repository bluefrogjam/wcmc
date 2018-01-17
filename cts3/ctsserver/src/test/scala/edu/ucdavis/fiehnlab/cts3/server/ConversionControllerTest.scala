package edu.ucdavis.fiehnlab.cts3.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 1/17/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest
class ConversionControllerTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val controller: ConversionController = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionControllerTest" should {

    "return a Sequence of Hits" in {
      val result = controller.convert("name", "inchikey", "alanine")

      result shouldBe a[Seq[Hit]]
      result.size should be > 0
      result.head.result shouldEqual "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N"
    }

    "return nempty sequence of hits" in {
      val result = controller.convert("name", "inchikey", "doesn't exist")

      result shouldBe a[Seq[Hit]]
      result.isEmpty

      logger.debug(s"RESULT: ${result}")
    }
  }
}
