package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]),
  scanBasePackages = Array("edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy", "edu.ucdavis.fiehnlab.wcmc.utilities.casetojson"))
class EntropyConfiguration {}
