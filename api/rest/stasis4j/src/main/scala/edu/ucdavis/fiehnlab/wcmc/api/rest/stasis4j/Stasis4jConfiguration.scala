package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j

import org.springframework.context.annotation.{ComponentScan, Configuration}

@Configuration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j", "edu.ucdavis.fiehnlab.wcmc.utilities.casetojson"))
class Stasis4jConfiguration {}
