package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}

@Configuration
@ComponentScan
@Profile(Array("carrot.processing.peakdetection"))
class MSDialConfiguration
