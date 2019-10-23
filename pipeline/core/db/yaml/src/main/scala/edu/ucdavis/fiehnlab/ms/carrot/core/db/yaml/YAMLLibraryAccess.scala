package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import java.util

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.ReadonlyLibrary
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class YAMLLibraryAccess @Autowired()(properties: YAMLLibraryConfigurationProperties, loader: ResourceLoader) extends ReadonlyLibrary[Target] with Logging {

  logger.info(s"Using ${loader.getClass.getSimpleName} to load targets from ${properties.resource}")
  private val data = new Yaml().loadAll(loader.load(properties.resource).get).asScala.collect {
    //  private val data = new Yaml().loadAll(getClass.getResourceAsStream(properties.resource)).asScala.collect {
    case config: util.Map[String, java.util.List[Any]] =>
      config.asScala.collect {
        case (key: String, value: java.util.List[Any]) if key == "config" =>
          value.asScala.collect {

            //we are now on the method level
            case method: util.Map[String, Any] =>

              val name = method.get("name").asInstanceOf[String]
              val description = method.get("description").asInstanceOf[String]
              val column = method.get("column").asInstanceOf[String]
              val ionmode = method.get("ionMode") match {
                case "positive" => Some(PositiveMode())
                case "negative" => Some(NegativeMode())
                case _ => None
              }

              val instrument = method.get("instrument").toString

              val acquisitionMethod = AcquisitionMethod(
                ChromatographicMethod(
                  name,
                  Some(instrument),
                  Some(column),
                  ionmode
                )
              )

              if (method.get("targets") != null) {
                val targets = method.get("targets").asInstanceOf[java.util.List[Any]].asScala.zipWithIndex.collect {

                  //we are at the target level
                  case (target: java.util.Map[String, Any], index: Int) =>
                    //annotation target
                    if (target.get("isInternalStandard") == null | target.get("isInternalStandard") == false) {
                      try {
                        new AnnotationTarget {
                          override val idx: Int = index

                          /**
                            * a name for this spectra
                            */
                          override var name: Option[String] = Some(target.get("identifier").asInstanceOf[String])
                          /**
                            * retention time in seconds of this target
                            */
                          override val retentionIndex: Double = target.get("retentionTimeUnit") match {
                            case "minutes" => BigDecimal(target.get("retentionTime").asInstanceOf[Double]).setScale(2,
                              BigDecimal.RoundingMode.CEILING).toDouble * 60
                            case "seconds" => BigDecimal(target.get("retentionTime").asInstanceOf[Double]).setScale(2,
                              BigDecimal.RoundingMode.CEILING).toDouble
                          }
                          /**
                            * the unique inchi key for this spectra
                            */
                          override var inchiKey: Option[String] = None
                          /**
                            * the mono isotopic mass of this spectra
                            */
                          override val precursorMass: Option[Double] =
                            Some(BigDecimal(target.get("accurateMass").toString).setScale(5, BigDecimal.RoundingMode.CEILING).toDouble)
                          /**
                            * unique mass for a given target
                            */
                          override val uniqueMass: Option[Double] = None
                          /**
                            * is this a confirmed target
                            */
                          override var confirmed: Boolean = target.get("confirmed").asInstanceOf[Boolean]
                          /**
                            * is this target required for a successful retention index correction
                            */
                          override var requiredForCorrection: Boolean = false
                          /**
                            * is this a retention index correction standard
                            */
                          override var isRetentionIndexStandard: Boolean = false
                          /**
                            * associated spectrum propties if applicable
                            */
                          override val spectrum: Option[SpectrumProperties] = None
                        }
                      } catch {
                        case ex: NumberFormatException => println(target)
                          throw ex
                      }
                    }
                    //correction target
                    else {
                      new CorrectionTarget {
                        override val idx: Int = index

                        /**
                          * a name for this spectra
                          */
                        override var name: Option[String] = Some(target.get("identifier").toString)
                        /**
                          * retention time in seconds of this target
                          */
                        override val retentionIndex: Double = target.get("retentionTimeUnit") match {
                          case "minutes" => BigDecimal(target.get("retentionTime").asInstanceOf[Double]).setScale(2,
                            BigDecimal.RoundingMode.CEILING).toDouble * 60
                          case "seconds" => BigDecimal(target.get("retentionTime").asInstanceOf[Double]).setScale(2,
                            BigDecimal.RoundingMode.CEILING).toDouble
                        }
                        /**
                          * the unique inchi key for this spectra
                          */
                        override var inchiKey: Option[String] = None
                        /**
                          * the mono isotopic mass of this spectra
                          */
                        override val precursorMass: Option[Double] =
                          Some(BigDecimal(target.get("accurateMass").toString).setScale(5, BigDecimal.RoundingMode.CEILING).toDouble)
                        /**
                          * unique mass for a given target
                          */
                        override val uniqueMass: Option[Double] = None
                        /**
                          * is this a confirmed target
                          */
                        override var confirmed: Boolean = target.get("confirmed").asInstanceOf[Boolean]
                        /**
                          * is this target required for a successful retention index correction
                          */
                        override var requiredForCorrection: Boolean = target.get("requiredForCorrection").asInstanceOf[Boolean]
                        /**
                          * is this a retention index correction standard
                          */
                        override var isRetentionIndexStandard: Boolean = true
                        /**
                          * associated spectrum propties if applicable
                          */
                        override val spectrum: Option[SpectrumProperties] = None
                      }
                    }
                }

                //return acqusition method with associated targets
                acquisitionMethod -> targets.toList
              }
          }
      }.flatten
  }.flatten.asInstanceOf[List[(AcquisitionMethod, List[Target])]].groupBy(_._1).mapValues(_.map(_._2))

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[Target] = this.data(acquisitionMethod).flatten

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = this.data.keys.toList

}

@Component
@Profile(Array("carrot.targets.yaml.correction"))
class YAMLCorrectionLibraryAccess @Autowired()(parent: YAMLLibraryAccess) extends ReadonlyLibrary[CorrectionTarget] {

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[CorrectionTarget] = parent.load(acquisitionMethod).collect {
    case x: CorrectionTarget => x
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = parent.libraries
}


@Component
@Profile(Array("carrot.targets.yaml.annotation"))
class YAMLAnnotationLibraryAccess @Autowired()(parent: YAMLLibraryAccess) extends ReadonlyLibrary[AnnotationTarget] {

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[AnnotationTarget] = parent.load(acquisitionMethod).collect {
    case x: AnnotationTarget => x
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = parent.libraries
}
