package edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest.FunSuite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
/**
  * Created by wohlgemuth on 10/11/17.
  */
@SpringBootTest
class CaseClassToJSONSerializationAutoConfigurationTest extends FunSuite {

  @Autowired
  val objectMapper:ObjectMapper = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  test("testObjectMapper") {
    assert(objectMapper != null)

    val out = new ByteArrayOutputStream()
    objectMapper.writeValue(out,TestToSerialize("tada"))

    val in = objectMapper.readValue(new ByteArrayInputStream(out.toByteArray),classOf[TestToSerialize])

    assert(in.name == "tada")
  }

}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CaseClassToJSONSerializationAutoConfigurationTestConfig

case class TestToSerialize(name:String)