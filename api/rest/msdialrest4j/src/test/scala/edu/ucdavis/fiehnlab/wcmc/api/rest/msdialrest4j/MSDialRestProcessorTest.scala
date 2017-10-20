package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io.{File, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
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

  @Autowired
  val fserv4jCli: FServ4jClient = null

  def sha256Hash(text: String): String = String.format("%064x", new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialRestProcessorTest" ignore {

    "upload a file to the server" in {
      val filename = "testA.d"
      if (!fserv4jCli.exists(filename)) {
        fail(s"can't load ${filename}")
      }

      val file: File = new File(s"${filename}.zip")
      val source = fserv4jCli.download(filename).getOrElse(fail(s"can't download ${filename}"))
      val target = new FileOutputStream(file)
      try {
        IOUtils.copy(source, target)
      } finally {
        target.flush()
        target.close()
      }

      val (uploadRes, token) = mSDialRestProcessor.upload(file)

      uploadRes shouldEqual sha256Hash("testA.d").substring(0, 9)
      token shouldEqual "testA.d"
    }

    "process" must {
      "process a .d file" in {

        val input = new File("testA.d")

        val output = mSDialRestProcessor.process(input)
        logger.warn(s"OUTPUT: ${output}")

        output shouldBe a[File]
        output.getName shouldEqual "testA.msdial"

        val resultLines = Source.fromFile(output).getLines().toSeq
        resultLines.head.split("\t") should contain ("Name")
        resultLines.head.split("\t") should contain ("ScanAtLeft")

        resultLines.size should be (12)
      }

      //fails currently with a 500 error, need to wait till diego is back from vacation to fix this
      "process a .abf file" ignore { // this should only recieve raw data files from now on

        val input = new File("testA.abf")

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
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MSDialRestProcessorConfig {
}
