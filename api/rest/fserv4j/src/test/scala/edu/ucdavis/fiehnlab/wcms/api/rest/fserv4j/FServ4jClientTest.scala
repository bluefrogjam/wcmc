package edu.ucdavis.fiehnlab.wcms.api.rest.fserv4j

import java.io.{File, FileWriter}

import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.server.fserv.FServ
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 7/9/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT,classes = Array(classOf[FServ],classOf[FServ4jClientConfiguration]))
class FServ4jClientTest extends WordSpec with ShouldMatchers with BeforeAndAfterEach{

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
    }

    "test some other file extensions" should {

      for(x <- Array("xml","txt","abf","mzML","cdf")){

        for(a <- 0.to(10)) {
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
class FServ4jClientConfiguration{

  @Bean
  def restTemplate:RestTemplate = new RestTemplate()

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File("target/test2"))

}