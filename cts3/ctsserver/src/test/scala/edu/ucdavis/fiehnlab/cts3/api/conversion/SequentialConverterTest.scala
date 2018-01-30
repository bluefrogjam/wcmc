package edu.ucdavis.fiehnlab.cts3.api.conversion

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.Converter
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 1/30/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[SequentialConverterTestConfiguration]))
class SequentialConverterTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val cactusConverter: CactusConverter = null

  @Autowired
  val pubchemConverter: PubChemConverter = null

  @Autowired
  val seqConverter: SequentialConverter = null

  @Autowired
  val objectMapper: ObjectMapper = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SequentialConverter" should {
    "is defined" in {
      seqConverter shouldBe a[Converter]
      seqConverter shouldBe a[SequentialConverter]
    }

    "have converters" in {
      seqConverter.converters should not be empty
      seqConverter.converters should have size 2
    }

    "combine all the source types" in {
      val exp = cactusConverter.requires ++ pubchemConverter.requires
      val sources = seqConverter.requires
      logger.debug(s"${sources}")
      logger.debug(s"${exp}")

      sources shouldEqual (exp)
    }

    "combine all the destination types" in {
      val exp = cactusConverter.provides ++ pubchemConverter.provides
      val dests = seqConverter.provides
      logger.debug(s"${dests}")
      logger.debug(s"${exp}")

      dests shouldEqual (exp)
    }
  }
}

@Configuration
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class SequentialConverterTestConfiguration() {
  @Bean
  def seqConverter(@Autowired cactusConverter: CactusConverter, @Autowired pubChemConverter: PubChemConverter) = new SequentialConverter(List(cactusConverter, pubChemConverter))
}
