package edu.ucdavis.fiehnlab.wcmc.utilities.translate.impl

import java.io.{File, FileWriter}
import java.util

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.wcmc.utilities.translate.api.LibraryTranslator
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.translate.excel"))
class ExcelLibraryTranslator @Autowired()(storage: ResourceStorage) extends LibraryTranslator with Logging {

  override def convertToFile(from: LibraryAccess[_ <: Target], method: AcquisitionMethod): Unit = {
    val methods = from.libraries

    val yamlFile = YamlLibraryFile(methods.collect {
      case lib: AcquisitionMethod =>
        val targets = from.load(lib)
        logger.info(s"${lib.toString} -> $targets")

        val yamlTargets = targets.filter(_.isRetentionIndexStandard).collect {
          case target: Target =>
            YamlTarget(target.name.get,
              target.accurateMass.get,
              target.retentionTimeInMinutes,
              "minutes",
              target.isRetentionIndexStandard,
              target.requiredForCorrection,
              target.confirmed)
        }

        YamlLibrary(
          lib.chromatographicMethod.name,
          "",
          lib.chromatographicMethod.instrument.get,
          lib.chromatographicMethod.column.get,
          lib.chromatographicMethod.ionMode.get.mode,
          new util.ArrayList(yamlTargets.asJavaCollection)
        )
    }.asJava)

    val tmp = File.createTempFile(s"${method.chromatographicMethod.name}_${method.chromatographicMethod.ionMode.get.mode.substring(0, 3)}", ".yaml", new File(System.getProperty("java.io.tmpdir")))
    tmp.deleteOnExit()

    new Yaml().dump(YamlLibraryFile, new FileWriter(tmp))
    storage.store(tmp)
  }

  override def convert(from: LibraryAccess[_ <: Target], to: LibraryAccess[_ <: Target], method: AcquisitionMethod): Unit = {
    throw new UnsupportedOperationException("Cannot convert between the specified libraries.")
  }
}

case class YamlLibraryFile(
                              config: java.util.List[YamlLibrary]
                          )

case class YamlLibrary(
                          name: String,
                          description: String = "",
                          instrument: String,
                          column: String,
                          ionMode: String,
                          targets: java.util.List[YamlTarget]
                      )

case class YamlTarget(
                         identifier: String,
                         accurateMass: Double,
                         retentionTime: Double,
                         retentionTimeUnit: String,
                         isInternalStandard: Boolean = false,
                         requiredForCorrection: Boolean = false,
                         confirmed: Boolean = false
                     )
