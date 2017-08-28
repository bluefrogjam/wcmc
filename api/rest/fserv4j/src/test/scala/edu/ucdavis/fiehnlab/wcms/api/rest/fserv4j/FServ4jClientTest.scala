package edu.ucdavis.fiehnlab.wcms.api.rest.fserv4j

import java.io.{File, FileWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.LocalLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.server.fserv.FServ
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.io.Source

/**
  * Created by wohlgemuth on 7/9/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Array(classOf[FServ], classOf[FServ4jClientConfiguration]))
class FServ4jClientTest extends WordSpec with ShouldMatchers with BeforeAndAfterEach with LazyLogging {

  override protected def beforeEach(): Unit = {
    new File(s"$directory/test.txt").delete()
  }

  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @Autowired
  val fserv: FServ4jClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "FServ4jClientTest" should {

    "upload. exist and download" in {

      fserv.exists("test.txt") should be(false)

      fserv.upload(new File("src/test/resources/test.txt"))

      fserv.exists("test.txt") should be(true)

      val res = fserv.download("test.txt")

      res.isDefined should be(true)

      Source.fromInputStream(res.get).getLines().toSeq.size should be(Source.fromFile(new File("src/test/resources/test.txt")).getLines().toSeq.size)

    }

    "upload. exist and download as file" in {

      fserv.upload(new File("src/test/resources/test.txt"))

      fserv.exists("test.txt") should be(true)

      val res = fserv.loadAsFile("test.txt")

      res.isDefined should be(true)

      logger.info(s"file is stored at: ${res.get.getAbsolutePath}")
      Source.fromFile(res.get).getLines().toSeq.size should be(Source.fromFile(new File("src/test/resources/test.txt")).getLines().toSeq.size)

    }

    "upload a very large file" should {

      for (x <- 1 to 100 by 10) {
        s"test file size ${x}MB" in {
          import java.io.RandomAccessFile
          val temp = File.createTempFile("temp", "x")
          temp.deleteOnExit()
          val f = new RandomAccessFile(temp, "rw")
          f.setLength(1024 * 1024 * x)

          f.close()


          fserv.upload(temp)

          fserv.exists(temp.getName) should be(true)

          val res = fserv.loadAsFile(temp.getName)

          res.isDefined should be(true)

          logger.info(s"file is stored at: ${res.get.getAbsolutePath}")
          Source.fromFile(res.get).getLines().toSeq.size should be(Source.fromFile(temp).getLines().toSeq.size)

        }

      }
    }


    "test some other file extensions" should {

      for (x <- Array("xml", "txt", "abf", "mzML", "cdf")) {

        for (a <- 0.to(10)) {
          s"upload and download ${x} extension ($a)" in {
            val file = File.createTempFile("dadssa", s".$x")
            val writer = new FileWriter(file)
            for (i <- 0.to(1024)) {
              for (y <- 0.to(100)) {
                writer.append("a")
              }
            }
            writer.close()
            fserv.upload(file)

            fserv.exists(file.getName) should be(true)

            val res = fserv.download(file.getName)

            res.isDefined should be(true)
          }
        }
      }
    }

  }
}

@Configuration
@ComponentScan
class FServ4jClientConfiguration {

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File("target/test2"))

}