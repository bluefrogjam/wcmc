package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._

@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.lcms.config"))
@ComponentScan
class LCMSAnnotationTargetConfiguration extends Logging {


}
