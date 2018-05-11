package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]),
  scanBasePackages = Array("edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j", "edu.ucdavis.fiehnlab.wcmc.utilities.casetojson"))
class Stasis4jConfiguration {}
