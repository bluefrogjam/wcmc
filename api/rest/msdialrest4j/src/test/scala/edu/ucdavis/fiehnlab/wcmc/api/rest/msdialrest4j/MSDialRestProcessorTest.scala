package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

import scala.io.Source

/**
  * Created by wohlgemuth on 6/16/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MSDialRestProcessorConfig]))
class MSDialRestProcessorTest extends WordSpec with Logging with Matchers with Eventually {

  @Autowired
  val mSDialRestProcessor: MSDialRestProcessor = null

  @Autowired
  val fserv4j: FServ4jClient = null

  @Autowired
  val restTemplate: RestTemplate = null

  def sha256Hash(text: String): String = String.format("%064x", new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialRestProcessorTest" should {

    "process" must {
      "process an Agilent .d file" in {
        val input = fserv4j.loadAsFile("testA.d.zip").get

        val output = mSDialRestProcessor.process(input)

        output.getName matches "msdial.*?deco"

        val resultLines = Source.fromFile(output).getLines().toSeq
        output.deleteOnExit()
        resultLines.head.split("\t") should contain("Name")
        resultLines.head.split("\t") should contain("ScanAtLeft")

        resultLines.size should be(12)
      }

      "process a .abf file" in {
        val input = fserv4j.loadAsFile("testA.abf").get
        val output = mSDialRestProcessor.process(input)

        output.getName matches "msdial.*?deco"

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain("Name")
        resultLines.head.split("\t") should contain("ScanAtLeft")

        resultLines.size should be(12)
      }

    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MSDialRestProcessorConfig {
  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient("testfserv.fiehnlab.ucdavis.edu")
}
