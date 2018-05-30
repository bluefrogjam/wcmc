package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.correction

import java.util

import javax.validation.Valid
import javax.validation.constraints.{NotBlank, NotEmpty, Size}
import org.springframework.boot.context.properties.NestedConfigurationProperty

import scala.beans.BeanProperty

class GCMSLibraryConfiguration {

  @BeanProperty
  @Valid
  @Size(min = 1)
  @NestedConfigurationProperty
  val targets: java.util.List[GCMSRetentionIndexTargetConfiguration] = new util.ArrayList[GCMSRetentionIndexTargetConfiguration]()

  @BeanProperty
  @NotBlank
  var name: String = ""

  @BeanProperty
  @NotBlank
  var description: String = ""

  @BeanProperty
  @NotBlank
  var column: String = ""

  @BeanProperty
  @NotBlank
  var instrument: String = ""
  /**
    * how high do peaks have to be to be considered as targets
    * for RI correction
    */
  @BeanProperty
  var minimumPeakIntensity: Float = 0

  /**
    * which base peaks do we allow
    */
  @BeanProperty
  @NotEmpty
  var allowedBasePeaks: java.util.List[Double] = new util.ArrayList[Double]()
  /**
    * what is our allowed mass accuracy
    * if 0, we assume we are running in nominal mass mode!
    */
  var massAccuracy: Double = 0.0

  /**
    * helper method to check for nominal mass
    *
    * @return
    */
  def isNominal(): Boolean = massAccuracy.equals(0.0)
}
