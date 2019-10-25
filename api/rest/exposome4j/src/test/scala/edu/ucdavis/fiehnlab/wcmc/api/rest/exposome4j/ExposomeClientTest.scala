package edu.ucdavis.fiehnlab.wcmc.api.rest.exposome4j

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[ExposomeClientConfig]))
class ExposomeClientTest extends WordSpec {

  @Autowired
  val client: ExposomeClient = null
  new TestContextManager(this.getClass).prepareTestInstance(this)
  "ExposomeClientTest" should {

    "loadByInchiKey" in {
      client.loadByInchiKey("ORXQGKIUCDPEAJ")
    }

    "loadByName" in {
      val result = client.loadByName("Alanine")
    }

  }
}

@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class ExposomeClientConfig {

}