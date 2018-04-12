package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.annotation

import java.util
import javax.validation.Valid
import javax.validation.constraints.{DecimalMax, DecimalMin, NotBlank, Size}

import org.springframework.boot.context.properties.{ConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.Profile

import scala.beans.BeanProperty

@Profile(Array("carrot.gcms"))
@ConfigurationProperties(prefix = "carrot.gcms.annotation", ignoreUnknownFields = false, ignoreInvalidFields = false)
class GCMSAnnotationProperties {
  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var config: java.util.List[GCMSAnnotationProperties] = new util.ArrayList[GCMSAnnotationProperties]()
}00

/**
  * configuration for a specific gcms annotation library
  */
class GCMSAnnotationLibraryProperties {

  @BeanProperty
  @NotBlank
  var name: String = null

  @BeanProperty
  @NotBlank
  var instrument: String = null

  @BeanProperty
  @NotBlank
  var descritpion: String = "none provided"

  @BeanProperty
  @NotBlank
  var column: String = null

  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var sifterFilterProperties: java.util.List[SifterFilterProperties] = new util.ArrayList[SifterFilterProperties]()
}

/**
  * properties for a sifter filters
  * this should be in a list
  * to cover different conditions
  */
class SifterFilterProperties {

  @BeanProperty
  @DecimalMin("0.0")
  var minPurity: Double = 0.0

  @BeanProperty
  @DecimalMin("0.0")
  var maxPurity: Double = Double.MaxValue


  @BeanProperty
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  var minSimilarity: Double = 0.0

  @BeanProperty
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  var maxSimilarity: Double = 1.0

  @BeanProperty
  @DecimalMin("0.0")
  var minSignalNoise: Double = 0.0

  @BeanProperty
  @DecimalMin("0.0")
  var maxSignalNoise: Double = Double.MaxValue

}