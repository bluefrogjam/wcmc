package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import java.io.File

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
      "process a .abf file" ignore {

        val input = new File(getClass.getResource("/test.abf").toURI)

        val output = mSDialRestProcessor.process(input)

        val resultLines = Source.fromFile(output).getLines().toSeq

        resultLines.head.split("\t") should contain ("Name")
        resultLines.head.split("\t") should contain ("ScanAtLeft")

        resultLines.size should be (679)
      }

    }

  }
}

@SpringBootApplication
class MSDialRestProcessorConfig{

  @Bean
  def objectMapper: ObjectMapper = {

    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)

    //required, in case we are provided with a list of value
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

    mapper
  }

  @Bean
  def restOperations: RestOperations = {

    val rest: RestTemplate = new RestTemplate()
    rest.getMessageConverters.add(0, mappingJacksonHttpMessageConverter)
    rest
  }


  @Bean
  def mappingJacksonHttpMessageConverter: MappingJackson2HttpMessageConverter = {
    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(objectMapper)
    converter
  }

}