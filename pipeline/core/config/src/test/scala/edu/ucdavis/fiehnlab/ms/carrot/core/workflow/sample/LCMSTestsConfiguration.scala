package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class LCMSTestsConfiguration
