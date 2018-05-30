package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._

@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.lcms"))
@ComponentScan
class LCMSCorrectionTargetConfiguration {

  /**
    * defines a library access method, based on all methods
    * defines in the yaml file
    *
    * @param properties
    * @return
    */
  @Bean
  def correctionTargets(properties: LCMSCorrectionLibraryProperties): LibraryAccess[LCMSCorrectionTarget] = {

    val libs = properties.config.asScala.map { x =>

      new ReadonlyLibrary[LCMSCorrectionTarget] {
        override def load(acquisitionMethod: AcquisitionMethod): Iterable[LCMSCorrectionTarget] = {

          val method = acquisitionMethod.chromatographicMethod

          method.column match {
            case Some(column) if column.equals(x.column) =>
              method.instrument match {
                case Some(instrument) if instrument.equals(x.instrument) =>
                  x.targets.asScala.map(LCMSCorrectionTarget)
                case _ =>
                  Seq.empty
              }

            case _ => Seq.empty
          }
        }

        override def libraries: Seq[AcquisitionMethod] = Seq.empty
      }.asInstanceOf[LibraryAccess[LCMSCorrectionTarget]]
    }.toSeq.asJava

    new DelegateLibraryAccess[LCMSCorrectionTarget](libs)
  }

}









