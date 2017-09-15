package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.RemoteLoader
import edu.ucdavis.fiehnlab.server.fserv.FServ
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, ComponentScan}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

/**
	* Created by diego on 8/31/2017.
	*/
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Array(classOf[FServ], classOf[DataFormerClientConfiguration]))
class DataFormerClientTests extends WordSpec with ShouldMatchers with LazyLogging {
	@Autowired
	val dfClient: DataFormerClient = null

	@Autowired
	val fserv: FServ4jClient = null

	@Autowired
	val resourceLoader: RemoteLoader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" should {
		"upload a raw data file" in {
			val rawfile = resourceLoader.loadAsFile("B5_P20Lipids_Pos_QC029.d.zip")
			rawfile should not be null

			val result = dfClient.upload(rawfile.get)
			result should contain "uploaded successfully"

		}
	}
}
