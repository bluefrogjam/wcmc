package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.ApiConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Profile

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[ApiConfig]))
@EnableConfigurationProperties
@Profile(Array("carrot.gcms"))
class GCMSTestsConfiguration
