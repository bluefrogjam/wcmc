package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, Target}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._


@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.gcms"))
@ComponentScan
class GCMSCorrectionTargetConfiguration {

  /**
    * defines a library access method, based on all methods
    * defines in the yaml file
    *
    * @param properties
    * @return
    */
  @Bean
  def correctionTargets(properties: GCMSCorrectionLibraryProperties): LibraryAccess[CorrectionTarget] = {

    val libs = properties.config.asScala.map { x =>

      new ReadonlyLibrary[GCMSCorrectionTarget] {
        override def load(acquisitionMethod: AcquisitionMethod): Iterable[GCMSCorrectionTarget] = {

          val method = acquisitionMethod.chromatographicMethod


          method.column match {
            case Some(column) if column.equals(x.column) =>
              method.instrument match {
                case Some(instrument) if instrument.equals(x.instrument) =>
                  x.targets.asScala.map(GCMSCorrectionTarget)
                case _ =>
                  Seq.empty
              }

            case _ => Seq.empty
          }
        }

        override def libraries: Seq[AcquisitionMethod] = Seq.empty
      }.asInstanceOf[LibraryAccess[CorrectionTarget]]
    }.toSeq.asJava

    new DelegateLibraryAccess[CorrectionTarget](libs)
  }

}









