package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import java.io.File

import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.io.Source

/**
  * Created by wohlgemuth on 6/16/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MSDialRestProcessorConfig]))
class MSDialRestProcessorTest extends WordSpec with ShouldMatchers{

  @Autowired
  val mSDialRestProcessor:MSDialRestProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialRestProcessorTest" should {

    "process" must {

      "process a .d file" in {

        val input = new File(getClass.getResource("/test.d").toURI)

        val output = mSDialRestProcessor.process(input)

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain ("Name")
        resultLines.head.split("\t") should contain ("ScanAtLeft")

        resultLines.size should be (12)
      }

      //fails currently with a 500 error, need to wait till diego is back from vacation to fix this
      "process a .abf file" in {

        val input = new File(getClass.getResource("/test.abf").toURI)

        val output = mSDialRestProcessor.process(input)

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain ("Name")
        resultLines.head.split("\t") should contain ("ScanAtLeft")

        resultLines.size should be (12)
      }

    }

  }
}

@SpringBootApplication
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class MSDialRestProcessorConfig {
}
