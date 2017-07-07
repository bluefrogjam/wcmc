package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import java.io.{File, FileInputStream}

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation._
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
 * Created by diego on 2/7/2017.
 */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ContextConfiguration(classes = Array(classOf[MsdialPreProcessorTestConfiguration]))
@ActiveProfiles(Array("msdial"))
class MsdialPreProcessorTest extends WordSpec with LazyLogging {
  @Autowired
  val msdialPreProcessor: MsdialPreProcessor = null

  @Autowired
  val client: MSDialRestProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MsdialPreProcessor " should {
    val rawFile = "B5_SA0023_P20Lipids_Pos_1FL_1100.abf"

    "preProcess an agilent file " in {
      val res = msdialPreProcessor.doProcess(new Sample {
        override val fileName: String = rawFile
        override val spectra: Seq[_ <: MSSpectra] = Seq.empty
      })

      assert(res.isInstanceOf[MSDialSample])

      assert(res != null)
      assert(res.spectra.nonEmpty)
    }
  }
}

@Configuration
@Import(Array(classOf[TargetedWorkflowTestConfiguration]))
@Profile(Array("msdial"))
class MsdialPreProcessorTestConfiguration {

  @Bean
  def client: MSDialRestProcessor = {
    new MSDialRestProcessor()
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
}
