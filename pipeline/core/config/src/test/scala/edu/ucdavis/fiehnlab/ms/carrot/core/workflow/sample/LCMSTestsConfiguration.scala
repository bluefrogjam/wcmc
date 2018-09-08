package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableConfigurationProperties
class LCMSTestsConfiguration
