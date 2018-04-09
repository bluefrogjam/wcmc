package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage

import java.util

import com.github.wonwoo.dynamodb.test.autoconfigure.DynamoTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.store.result.dynamo"))
class DynamoSampleRepositoryTest extends WordSpec {

  @Autowired
  val dynamoSampleRepository: DynamoSampleRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DynamoSampleRepositoryTest" should {

    "create a new object" in {
      val list = new util.HashMap[String,Annotation]()
      list.put("tada",Annotation(12.0))
      list.put("todo",Annotation(12.0))
      val sample = DynamoSample("test", "test.txt", list)

      dynamoSampleRepository.save(sample)

    }
    "findAll" in {

    }

    "findByFileName" in {

    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class DynamoDBTest