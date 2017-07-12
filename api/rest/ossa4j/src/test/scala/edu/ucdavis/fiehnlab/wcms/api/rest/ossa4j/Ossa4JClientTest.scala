package edu.ucdavis.fiehnlab.wcms.api.rest.ossa4j

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by wohlgemuth on 6/29/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Ossa4jClientConfig]))
class Ossa4JClientTest extends WordSpec with ShouldMatchers{

  @Autowired
  val ossa4jClient: Ossa4JClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Ossa4JClientTest" should {

    "execute the following procedures" must {

      "be able to execute searches" in {

        ossa4jClient.clear

        ossa4jClient.librarySize should be(0)

        ossa4jClient.addLibrarySpectrum("1","10:1 11:2 13:3 14:4")

        ossa4jClient.librarySize should be(1)

        ossa4jClient.commit

        ossa4jClient.search("10:1 11:2 13:3 14:4",0.5f).size should be (1)
      }

    }
  }
}

@SpringBootApplication
class Ossa4jClientConfig {

}