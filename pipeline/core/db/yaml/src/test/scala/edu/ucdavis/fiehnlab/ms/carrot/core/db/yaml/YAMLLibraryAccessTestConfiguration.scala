package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import java.util

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.ClasspathResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

import scala.collection.JavaConverters._

@EnableConfigurationProperties
@Configuration
@ComponentScan
@Profile(Array("test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class YAMLLibraryAccessTestConfiguration {
  @Autowired
  val corrLib: YAMLCorrectionLibraryAccess = null

  @Autowired
  val annLib: YAMLAnnotationLibraryAccess = null

  @Bean
  def libraryAccess: MergeLibraryAccess = new MergeLibraryAccess(
    new DelegateLibraryAccess[CorrectionTarget](new util.ArrayList(Seq(corrLib).asJavaCollection)),
    new DelegateLibraryAccess[AnnotationTarget](new util.ArrayList(Seq(annLib).asJavaCollection)))

  @Bean
  @Profile(Array("local"))
  def loalLoader: ResourceLoader = new ClasspathResourceLoader()
}
