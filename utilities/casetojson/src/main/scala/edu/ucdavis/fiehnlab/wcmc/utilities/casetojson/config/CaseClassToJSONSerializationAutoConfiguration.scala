package edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.LazyLogging
import org.springframework.boot.autoconfigure._
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.core.{Ordered => SpringOrdered}

/**
  * Created by wohlgemuth on 7/11/17.
  */
@Configuration
@AutoConfigureOrder(SpringOrdered.HIGHEST_PRECEDENCE)
class CaseClassToJSONSerializationAutoConfiguration extends LazyLogging {

  @Bean
  def objectMapper: ObjectMapper = {

    logger.info("creating custom object mapper...")
    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)

    //required, in case we are provided with a list of value
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

    mapper
  }

  @Primary
  @Bean
  def restTemplate(mappingJackson2HttpMessageConverter: MappingJackson2HttpMessageConverter): RestTemplate = {
    logger.info("creating custom template with Jackson for scala support")
    val rest: RestTemplate = new RestTemplate()
    rest.getMessageConverters.add(0, mappingJackson2HttpMessageConverter)
    rest
  }


  @Bean
  def mappingJacksonHttpMessageConverter(objectMapper: ObjectMapper): MappingJackson2HttpMessageConverter = {
    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(objectMapper)
    converter
  }

}
