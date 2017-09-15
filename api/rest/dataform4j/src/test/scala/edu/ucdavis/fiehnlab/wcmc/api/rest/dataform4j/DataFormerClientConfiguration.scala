package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import edu.ucdavis.fiehnlab.loader.RemoteLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan}
import org.springframework.web.client.RestTemplate

/**
	* Created by diego on 9/14/2017.
	*/
@SpringBootConfiguration
@ComponentScan
class DataFormerClientConfiguration {
	@Bean
	def dfClient: DataFormerClient = new DataFormerClient()

	@Bean
	def restTemplate: RestTemplate = new RestTemplate()

	@Bean
	def resourceLoader: RemoteLoader = new FServ4jClient()
}
