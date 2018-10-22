package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._

@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.lcms"))
@ComponentScan
class LCMSAnnotationTargetConfiguration extends LazyLogging {

  /**
    * defines a library access method, based on all methods
    * defines in the yaml file
    *
    * @param properties
    * @return
    */
  @Bean
  @Qualifier("staticAnnotationLibraryAccess")
  def annotationTargets(properties: LCMSAnnotationLibraryProperties): LibraryAccess[AnnotationTarget] = {
    logger.info("loading lcms annotation targets")
    val methods: Map[AcquisitionMethod, Iterable[LCMSAnnotationTarget]] = properties.config.asScala.map { x =>
      (AcquisitionMethod(ChromatographicMethod(x.name, Some(x.instrument), Some(x.column), x.ionMode match {
        case "positive" => Some(PositiveMode())
        case "negative" => Some(NegativeMode())
        case _ => None
      })), x.targets.asScala.map(LCMSAnnotationTarget))
    }.toMap


    val libs = methods.keySet.map { x =>
        logger.info(s"=== method ${x} ===")

      new ReadonlyLibrary[LCMSAnnotationTarget] {

        override def load(acquisitionMethod: AcquisitionMethod): Iterable[LCMSAnnotationTarget] = {
          if (acquisitionMethod == x) {
            methods(x)
          }
          else {
            Seq.empty
          }
        }

        override def libraries: Seq[AcquisitionMethod] = methods.keySet.toSeq
      }.asInstanceOf[LibraryAccess[AnnotationTarget]]
    }.toSeq.asJava

    new DelegateLibraryAccess[AnnotationTarget](libs)
  }

}
