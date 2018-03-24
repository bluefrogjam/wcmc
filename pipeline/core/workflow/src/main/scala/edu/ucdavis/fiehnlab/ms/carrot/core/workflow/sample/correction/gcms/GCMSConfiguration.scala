package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import java.util
import javax.validation.Valid
import javax.validation.constraints.{Max, Min, NotNull, Size}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import org.hibernate.validator.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

@EnableConfigurationProperties(Array(classOf[GCMSLibraryProperties]))
@Configuration
@Profile(Array("carrot.gcms"))
class GCMSTargetConfiguration {

  @Bean
  def correctionTargets(properties: GCMSLibraryProperties): LibraryAccess[GCMSRetentionIndexTarget] = {
/*

    val libs = properties.config.asScala.map { x =>

      new ReadonlyLibrary[GCMSRetentionIndexTarget] {
        override def load(acquisitionMethod: AcquisitionMethod): Iterable[GCMSRetentionIndexTarget] = x.targets.asScala

        override def libraries: Seq[AcquisitionMethod] = Seq.empty
      }.asInstanceOf[LibraryAccess[GCMSRetentionIndexTarget]]
    }.toSeq.asJava

    new DelegateLibraryAccess[GCMSRetentionIndexTarget](libs)
*/

    null
  }

}

@ConfigurationProperties(prefix ="carrot.gcms.correction")
@Profile(Array("carrot.gcms"))
@Validated
class GCMSLibraryProperties {

  /**
    * all our targets
    */
  @Valid
  @Size(min = 1)
  @BeanProperty
  val config: java.util.List[GCMSLibraryConfiguration] = new util.ArrayList

  /**
    * minimum found standards
    */
  @Min(1)
  @BeanProperty
  val requiredStandards: Int = 0

}

class GCMSLibraryConfiguration {

  @BeanProperty
  @Valid
  @Size(min = 1)
  val targets:java.util.List[GCMSRetentionIndexTarget] = null

  @BeanProperty
  @NotBlank
  val name:String = ""

  @BeanProperty
  @NotBlank
  val description:String = ""
}

class GCMSRetentionIndexTarget extends Target {
  @BeanProperty
  @NotBlank
  val identifier: String = null

  @Min(1)
  @BeanProperty
  val retentionIndex: Double = 0.0

  @Min(1)
  @BeanProperty
  val minApexSn: Double = 0.0

  @Min(1)
  @BeanProperty
  val qualifierIon: Double = 0.0

  @Min(0)
  @BeanProperty
  val minQualifierRatio: Double = 0.0

  @Min(0)
  @BeanProperty
  val maxQualifierRatio: Double = 0.0

  @Min(0)
  @BeanProperty
  val minDistanceRatio: Double = 0.0
  @Min(0)
  @BeanProperty
  val maxDistanceRatio: Double = 0.0
  @Min(0)
  @Max(1000)
  @BeanProperty
  val minSimilarity: Double = 0.0
  @NotNull
  @BeanProperty
  val required: Boolean = false
  @Min(0)
  @BeanProperty
  val uniqueMass: Double = 0.0

  @NotBlank
  @BeanProperty
  val spectra: String = null

  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = None
  /**
    * the mono isotopic mass of this spectra
    */
  lazy override val precursorMass: Option[Double] = None
  /**
    * is this a confirmed target
    */
  override var confirmed: Boolean = true
  /**
    * is this target required for a successful retention index correction
    */
  override var requiredForCorrection: Boolean = required
  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = true
  /**
    * associated spectrum propties if applicable
    */
  lazy override val spectrum: Option[SpectrumProperties] = Option(new SpectrumProperties() {
    /**
      * the msLevel of this spectra
      */
    override val msLevel: Short = 1
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = spectra.split(" ").map { x =>

      val v = x.split(":")
      Ion(v(0).toDouble, v(1).toDouble)
    }

  })
  /**
    * a name for this spectra
    */
  override var name: Option[String] = Some(identifier)
}