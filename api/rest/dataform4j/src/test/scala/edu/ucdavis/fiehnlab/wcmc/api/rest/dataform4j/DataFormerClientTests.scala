package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.server.fserv.{FServ, FServSecurity}
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

/**
	* Created by diego on 8/31/2017.
	*/
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[FServ], classOf[FServSecurity], classOf[DataFormerClientConfiguration]))
class DataFormerClientTests extends WordSpec with ShouldMatchers with LazyLogging {
	@Autowired
	val dfClient: DataFormerClient = null

	@Autowired
	val resourceLoader: RecursiveDirectoryResourceLoader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" ignore {
		"upload a raw data file" in {
			val rawfile = resourceLoader.loadAsFile("B5_P20Lipids_Pos_QC029.d.zip")
			rawfile should not be null

			val result = dfClient.upload(rawfile.get)
			logger.debug(s"result $result")
			result should contain("uploaded successfully")

		}
	}
}

@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class DataFormerClientConfiguration {
	@Bean
	def dfClient: DataFormerClient = new DataFormerClient()

	@Bean
	def restTemplate: RestTemplate = new RestTemplate()

	@Bean
	def fserv4j: FServ4jClient = new FServ4jClient()

	@Bean
	def resourceLoader: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("/h/p20repo"))
}
