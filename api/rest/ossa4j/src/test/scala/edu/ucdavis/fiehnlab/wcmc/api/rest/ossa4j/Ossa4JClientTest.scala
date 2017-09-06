package edu.ucdavis.fiehnlab.wcmc.api.rest.ossa4j

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 6/29/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Ossa4jClientConfig]))
class Ossa4JClientTest extends WordSpec with ShouldMatchers{

  @Autowired
  val ossa4jClient: Ossa4JClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Ossa4JClientTest" ignore {

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
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class Ossa4jClientConfig {

}