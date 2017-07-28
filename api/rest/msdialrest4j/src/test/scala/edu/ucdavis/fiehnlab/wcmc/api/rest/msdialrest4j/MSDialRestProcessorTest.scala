package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.utilities.SpectrumMinimizer
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.io.Source

/**
  * Created by wohlgemuth on 6/16/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MSDialRestProcessorConfig]))
class MSDialRestProcessorTest extends WordSpec with LazyLogging with ShouldMatchers {

  @Autowired
  val mSDialRestProcessor:MSDialRestProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialRestProcessorTest" should {

    "process" must {

      "process a .d file" in {

        val input = new File(getClass.getResource("/test.d").toURI)

        val output = mSDialRestProcessor.process(input)

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain ("Title")
        resultLines.head.split("\t") should contain ("PeakID")
	      resultLines.head.split("\t") should not contain ("ScanAtTop")

	      resultLines.length should be(12)
      }

      //fails currently with a 500 error, need to wait till diego is back from vacation to fix this
      "process a .abf file" in {

        val input = new File(getClass.getResource("/test.abf").toURI)

        val output = mSDialRestProcessor.process(input)

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain ("Title")
        resultLines.head.split("\t") should contain ("PeakID")
        resultLines.head.split("\t") should not contain ("ScanAtTop")

	      resultLines.length should be(12)
      }

    }

  }
}

@Configuration
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class MSDialRestProcessorConfig {
	@Bean
	def fserv4jcli: FServ4jClient = new FServ4jClient()

	@Bean
	def msdialProcessor: MSDialRestProcessor = new MSDialRestProcessor()

	@Bean
	def minimizer: SpectrumMinimizer = new SpectrumMinimizer()
}
