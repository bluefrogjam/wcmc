package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import org.springframework.context.annotation.{ComponentScan, Configuration, EnableAspectJAutoProxy, Profile}

@Configuration
@ComponentScan
@Profile(Array("carrot.processing.peakdetection"))
@EnableAspectJAutoProxy(proxyTargetClass = true)
class MSDialConfiguration
