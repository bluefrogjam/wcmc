package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}

@Configuration
@Profile(Array("carrot.lcms"))
@ComponentScan
@EnableConfigurationProperties
class LCMSAnnotationConfiguration
