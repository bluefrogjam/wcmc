package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._

@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.lcms"))
@ComponentScan
class LCMSCorrectionTargetConfiguration extends Logging {

  /**
    * defines a library access method, based on all methods
    * defines in the yaml file
    *
    * @param properties
    * @return
    */
  @Bean
  def correctionTargets(properties: LCMSCorrectionLibraryProperties): LibraryAccess[CorrectionTarget] = {

    val methods: Map[AcquisitionMethod, Iterable[LCMSCorrectionTarget]] = properties.config.asScala.map { x =>
      (AcquisitionMethod(ChromatographicMethod(x.name, Some(x.instrument), Some(x.column), x.ionMode match {
        case "positive" => Some(PositiveMode())
        case "negative" => Some(NegativeMode())
        case _ => None
      })), x.targets.asScala.map(LCMSCorrectionTarget))
    }.toMap


    val libs = methods.keySet.map { x =>
      logger.info(s"==== method ${x.chromatographicMethod} ====")

      new ReadonlyLibrary[LCMSCorrectionTarget] {

        override def load(acquisitionMethod: AcquisitionMethod): Iterable[LCMSCorrectionTarget] = {
          if (acquisitionMethod == x) {
            methods(x)
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









