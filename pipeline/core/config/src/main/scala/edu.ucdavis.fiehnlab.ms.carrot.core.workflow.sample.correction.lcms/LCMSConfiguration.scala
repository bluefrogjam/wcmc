//package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms
//
//import java.util
//
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, ReadonlyLibrary}
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
//import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.RatioConfiguration
//import javax.validation.Valid
//import javax.validation.constraints._
//import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties, NestedConfigurationProperty}
//import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
//import org.springframework.stereotype.Component
//import org.springframework.validation.annotation.Validated
//
//import scala.beans.BeanProperty
//import scala.collection.JavaConverters._
//
//
//@EnableConfigurationProperties
//@Configuration
//@Profile(Array("carrot.lcms"))
//@ComponentScan
//class LCMSCorrectionTargetConfiguration {
//
//  /**
//    * defines a library access method, based on all methods
//    * defines in the yaml file
//    *
//    * @param properties
//    * @return
//    */
//  @Bean
//  def correctionTargets(properties: LCMSCorrectionLibraryProperties): LibraryAccess[LCMSCorrectionTarget] = {
//
//    val libs = properties.config.asScala.map { x =>
//
//      new ReadonlyLibrary[LCMSCorrectionTarget] {
//        override def load(acquisitionMethod: AcquisitionMethod): Iterable[LCMSCorrectionTarget] = {
//
//          acquisitionMethod.chromatographicMethod match {
//            case Some(method) if method.name.equals(x.name) =>
//              method.column match {
//                case Some(column) if column.equals(x.column) =>
//                  x.targets.asScala.map(LCMSCorrectionTarget)
//                case None => Seq.empty
//              }
//
//            case None => Seq.empty
//          }
//
//        }
//
//        override def libraries: Seq[AcquisitionMethod] = Seq.empty
//      }.asInstanceOf[LibraryAccess[LCMSCorrectionTarget]]
//    }.toSeq.asJava
//
//    new DelegateLibraryAccess[LCMSCorrectionTarget](libs)
//  }
//
//}
//
//case class LCMSCorrectionTarget(target: LCMSRetentionIndexTargetConfiguration) extends Target {
//
//  val config: LCMSRetentionIndexTargetConfiguration = target
//  /**
//    * a name for this spectra
//    */
//  override var name: Option[String] = Option(target.identifier)
//  /**
//    * retention time in seconds of this target
//    */
//  override val retentionIndex: Double = target.retentionIndex
//  /**
//    * the unique inchi key for this spectra
//    */
//  override var inchiKey: Option[String] = None
//  /**
//    * the mono isotopic mass of this spectra
//    */
//  override val precursorMass: Option[Double] = None
//  /**
//    * is this a confirmed target
//    */
//  override var confirmed: Boolean = true
//  /**
//    * is this target required for a successful retention index correction
//    */
//  override var requiredForCorrection: Boolean = target.required
//  /**
//    * is this a retention index correction standard
//    */
//  override var isRetentionIndexStandard: Boolean = true
//  /**
//    * associated spectrum propties if applicable
//    */
//  override val spectrum: Option[SpectrumProperties] = Option(new SpectrumProperties {
//    /**
//      * a list of model ions used during the deconvolution
//      */
//    override val modelIons: Option[Seq[Double]] = None
//    /**
//      * all the defined ions for this spectra
//      */
//    override val ions: Seq[Ion] = target.spectra.split(" ").map { x =>
//
//      val v = x.split(":")
//      Ion(v(0).toDouble, v(1).toFloat)
//    }
//    /**
//      * the msLevel of this spectra
//      */
//    override val msLevel: Short = 1
//  })
//}
//
//@Component
//@Validated
//@Profile(Array("carrot.lcms"))
//@ConfigurationProperties(prefix = "carrot.lcms.correction", ignoreUnknownFields = false, ignoreInvalidFields = false)
//class LCMSCorrectionLibraryProperties {
//
//  /**
//    * all our targets
//    */
//  @Valid
//  @Size(min = 1)
//  @BeanProperty
//  @NestedConfigurationProperty
//  var config: java.util.List[LCMSLibraryConfiguration] = new util.ArrayList[LCMSLibraryConfiguration]()
//
//  /**
//    * minimum found standards
//    */
//  @Min(1)
//  @BeanProperty
//  var requiredStandards: Int = 6
//
//}
//
//class LCMSLibraryConfiguration {
//
//  @BeanProperty
//  @Valid
//  @Size(min = 1)
//  @NestedConfigurationProperty
//  val targets: java.util.List[LCMSRetentionIndexTargetConfiguration] = new util.ArrayList[LCMSRetentionIndexTargetConfiguration]()
//
//  @BeanProperty
//  @NotBlank
//  var name: String = ""
//
//  @BeanProperty
//  @NotBlank
//  var description: String = ""
//
//  @BeanProperty
//  @NotBlank
//  var column: String = ""
//
//  /**
//    * how high do peaks have to be to be considered as targets
//    * for RI correction
//    */
//  @BeanProperty
//  var minimumPeakIntensity: Float = 0
//
//  /**
//    * which base peaks do we allow
//    */
//  @BeanProperty
//  @NotEmpty
//  var allowedBasePeaks: java.util.List[Double] = new util.ArrayList[Double]()
//
//  /**
//    * what is our allowed mass accuracy
//    * if 0, we assume we are running in nominal mass mode!
//    */
//  var massAccuracy: Double = 0.0
//
//  /**
//    * helper method to check for nominal mass
//    *
//    * @return
//    */
//  def isNominal(): Boolean = massAccuracy.equals(0.0)
//}
//
//class LCMSRetentionIndexTargetConfiguration {
//  @BeanProperty
//  @NotBlank
//  var identifier: String = _
//
//  @BeanProperty
//  @DecimalMax("10000000.0")
//  @DecimalMin("0.0")
//  var retentionIndex: Double = 0.0
//
//  @BeanProperty
//  @DecimalMax("1000000.0")
//  @DecimalMin("0.0")
//  var minApexSn: Double = 0.0
//
//  @BeanProperty
//  @DecimalMax("5000.0")
//  @DecimalMin("0.0")
//  var qualifierIon: Double = 0.0
//
//  @BeanProperty
//  @DecimalMax("10.0")
//  @DecimalMin("-10.0")
//  var minQualifierRatio: Double = 0.0
//
//  @BeanProperty
//  @DecimalMax("10.0")
//  @DecimalMin("-10.0")
//  var maxQualifierRatio: Double = 0.0
//
//  @BeanProperty
//  @Valid
//  @Size(min = 1)
//  @NestedConfigurationProperty
//  val distanceRatios: java.util.List[RatioConfiguration] = new util.ArrayList[RatioConfiguration]()
//
//  @BeanProperty
//  @DecimalMax("1.0")
//  @DecimalMin("0.0")
//  var minSimilarity: Double = 0.0
//
//  @BeanProperty
//  @NotNull
//  var required: Boolean = false
//
//  @BeanProperty
//  @DecimalMax("5000.0")
//  @DecimalMin("0.0")
//  var uniqueMass: Double = 0.0
//
//  @BeanProperty
//  @NotBlank
//  var spectra: String = _
//
//}
