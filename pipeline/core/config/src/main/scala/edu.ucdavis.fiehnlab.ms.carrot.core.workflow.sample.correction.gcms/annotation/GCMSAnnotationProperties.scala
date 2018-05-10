package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.annotation

import java.util
import javax.validation.Valid
import javax.validation.constraints._

import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty

@EnableConfigurationProperties
@Configuration
@Profile(Array("carrot.gcms"))
@ComponentScan
class GCMSAnnotationConfigurations

@Validated
@Profile(Array("carrot.gcms"))
@ConfigurationProperties(prefix = "carrot.gcms.annotation", ignoreUnknownFields = false, ignoreInvalidFields = false)
@Component
class GCMSAnnotationProperties {

  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var config: java.util.List[GCMSAnnotationLibraryProperties] = new util.ArrayList[GCMSAnnotationLibraryProperties]()
}

/**
  * configuration for a specific gcms annotation library
  */
case class GCMSAnnotationLibraryProperties() {

  @BeanProperty
  @NotBlank
  @NotNull
  var instrument: String = null

  @BeanProperty
  @NotBlank
  @NotNull
  var description: String = "none provided"

  @BeanProperty
  @NotBlank
  var column: String = null

  @BeanProperty
  @NotBlank
  var deconvolution:String = "internal"

  @BeanProperty
  @NotBlank
  var version:String = ""

  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var filter: java.util.List[SifterFilterConfigurations] = new util.ArrayList[SifterFilterConfigurations]()
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

  override def toString = s"SifterFilterProperties(minPurity: $minPurity, maxPurity: $maxPurity, minSimilarity: $minSimilarity, maxSimilarity: $maxSimilarity, minSignalNoise: $minSignalNoise, maxSignalNoise: $maxSignalNoise)"
}

case class SifterFilterConfigurations(){

  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var matching: java.util.List[SifterFilterProperties] = new util.ArrayList[SifterFilterProperties]()

}