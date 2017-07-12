package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowConfig
import edu.ucdavis.fiehnlab.wcms.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 7/12/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[ABFSampleTestConfig], classOf[CaseClassToJSONSerializationConfiguration],classOf[WorkflowConfig]))
class ABFSampleTest extends WordSpec with ShouldMatchers {

  @Autowired
  val client: MSDialRestProcessor = null

  @Autowired
  val loader:FServ4jClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ABFSampleTest" should {

    "sblank sample" in {
      val name  = "B5_P20Lipids_Pos_Blank000.abf"
      val sample = new ABFSample(name,loader.loadAsFile(name).get,client)

      sample.spectra should not be empty
    }

    "qc sample" in {
      val name  = "B5_P20Lipids_Pos_QC000.abf"
      val sample = new ABFSample(name,loader.loadAsFile(name).get,client)

      sample.spectra should not be empty
    }


  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@ComponentScan(basePackageClasses = Array(classOf[MSDialRestProcessor],classOf[FServ4jClient]))
class ABFSampleTestConfig {

}