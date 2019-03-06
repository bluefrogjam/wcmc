package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, PositiveMode, Target}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._


@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.gcms"))
@ComponentScan
class GCMSCorrectionTargetConfiguration extends Logging{

  /**
    * defines a library access method, based on all methods
    * defines in the yaml file
    *
    * @param properties
    * @return
    */
  @Bean
  def correctionTargets(properties: GCMSCorrectionLibraryProperties): LibraryAccess[CorrectionTarget] = {

    logger.info("loading gcms correction targets")
    val methods: Map[AcquisitionMethod, Iterable[GCMSCorrectionTarget]] = properties.config.asScala.map { x =>
      (AcquisitionMethod(ChromatographicMethod(x.name, Some(x.instrument), Some(x.column), Some(PositiveMode()))), x.targets.asScala.map(GCMSCorrectionTarget))
    }.toMap


    val libs = methods.keySet.map { x =>


      new ReadonlyLibrary[GCMSCorrectionTarget] {

        override def load(acquisitionMethod: AcquisitionMethod): Iterable[GCMSCorrectionTarget] = {
          if (acquisitionMethod == x) {
            return methods(x)
          }
          else {
            Seq.empty
          }
        }

        override def libraries: Seq[AcquisitionMethod] = methods.keySet.toSeq
      }.asInstanceOf[LibraryAccess[CorrectionTarget]]
    }.toSeq.asJava

    new DelegateLibraryAccess[CorrectionTarget](libs)
  }

}









