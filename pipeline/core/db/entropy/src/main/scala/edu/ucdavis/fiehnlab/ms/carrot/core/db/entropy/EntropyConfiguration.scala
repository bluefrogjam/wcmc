package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Import

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class EntropyConfiguration {}
