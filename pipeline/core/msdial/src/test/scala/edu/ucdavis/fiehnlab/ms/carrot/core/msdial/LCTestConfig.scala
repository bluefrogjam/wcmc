package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableCaching
class LCTestConfig {
}
