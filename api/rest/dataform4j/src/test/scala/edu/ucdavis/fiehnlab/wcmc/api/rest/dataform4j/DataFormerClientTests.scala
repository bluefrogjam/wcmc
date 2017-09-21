package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.server.fserv.{FServ, FServSecurity}
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
	* Created by diego on 8/31/2017.
	*/
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Array(classOf[FServ], classOf[FServSecurity], classOf[DataFormerClientConfiguration]))
class DataFormerClientTests extends WordSpec with ShouldMatchers with LazyLogging {
	@Autowired
	val dfClient: DataFormerClient = null

	@LocalServerPort
	val port: Int = 0

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" should {
		"upload a raw data file" in {

			val result = dfClient.convert("B5_P20Lipids_Pos_QC029.d.zip")
			logger.debug(s"result $result")
			result should contain("{\"abfFile\":\"B5_P20Lipids_Pos_QC029.abf\", \"mzml\":\"B5_P20Lipids_Pos_QC029.mzml\"")

		}
	}
}

@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class DataFormerClientConfiguration {
	@Bean
	def dfClient: DataFormerClient = new DataFormerClient()

	//	@Bean
	//	def restTemplate: RestTemplate = new RestTemplate()

	@Bean
	def fserv4j: FServ4jClient = new FServ4jClient()

}
