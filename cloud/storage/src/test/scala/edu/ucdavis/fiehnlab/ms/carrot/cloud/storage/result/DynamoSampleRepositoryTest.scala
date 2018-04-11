package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage.result

import java.util

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class DynamoDBTest

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.store.result.dynamo"))
class DynamoSampleRepositoryTest extends WordSpec {

  @Autowired
  val dynamoSampleRepository: DynamoSampleRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DynamoSampleRepositoryTest" should {

    "create a new object" in {

    }
    "findAll" in {

    }

    "findByFileName" in {

    }

  }
}
