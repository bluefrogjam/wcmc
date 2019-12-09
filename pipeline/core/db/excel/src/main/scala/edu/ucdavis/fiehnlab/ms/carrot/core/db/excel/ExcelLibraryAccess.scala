package edu.ucdavis.fiehnlab.ms.carrot.core.db.excel

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.ReadonlyLibrary
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.apache.logging.log4j.scala.Logging
import org.apache.poi.ss.usermodel._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.targets.excel.correction", "carrot.targets.excel.annotation"))
class ExcelLibraryAccess @Autowired()(properties: ExcelLibraryConfigurationProperties, loader: ResourceLoader) extends ReadonlyLibrary[Target] {

  private val method = AcquisitionMethod(ChromatographicMethod(properties.libname, Some(properties.instrument),
    Some(properties.column), Some(properties.ionMode)))

  private val data = {
    val tgts = WorkbookFactory.create(loader.load(properties.filename).get).getSheetAt(0).asScala
        .zipWithIndex.filter(_._1.getRowNum > 0).collect {
      case (row: Row, idx: Int) =>
        val tgt =
          if (row.getCell(3).getStringCellValue.toBoolean) {
            new CorrectionTarget {
              override val idx: Int = row.getRowNum
              override var name: Option[String] = Some(row.getCell(0).getStringCellValue)
              override val retentionIndex: Double = BigDecimal(row.getCell(2)
                  .getNumericCellValue).setScale(2, BigDecimal.RoundingMode.CEILING).toDouble
              override var inchiKey: Option[String] = None
              override val precursorMass: Option[Double] = Some(BigDecimal(row.getCell(1)
                  .getNumericCellValue).setScale(5, BigDecimal.RoundingMode.CEILING).toDouble)
              override val uniqueMass: Option[Double] = None
              override var confirmed: Boolean = true
              override var requiredForCorrection: Boolean = true
              override var isRetentionIndexStandard: Boolean = true
              override val spectrum: Option[SpectrumProperties] = None
            }
          } else {
            new AnnotationTarget {
              override val idx: Int = row.getRowNum
              override var name: Option[String] = Some(row.getCell(0).getStringCellValue)
              override val retentionIndex: Double = BigDecimal(row.getCell(2)
                  .getNumericCellValue).setScale(2, BigDecimal.RoundingMode.CEILING).toDouble
              override var inchiKey: Option[String] = None
              override val precursorMass: Option[Double] = Some(BigDecimal(row.getCell(1)
                  .getNumericCellValue).setScale(5, BigDecimal.RoundingMode.CEILING).toDouble)
              override val uniqueMass: Option[Double] = None
              override var confirmed: Boolean = true
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = false
              override val spectrum: Option[SpectrumProperties] = None
            }
          }
        tgt
    }
    tgts
  }

  /**
   * loads all the spectra from the library
   * applicable for the given acquisition method
   *
   * @return
   */
  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[Target] = {
    if (acquisitionMethod == method) {
      confirmed match {
        case Some(x) =>
          data.filter(_.confirmed == x)
        case _ => Seq.empty
      }
    } else {
      Seq.empty
    }
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = {
    Seq(method)
  }
}


@Component
@Profile(Array("carrot.targets.excel.correction"))
class ExcelCorrectionLibraryAccess @Autowired()(parent: ExcelLibraryAccess) extends ReadonlyLibrary[CorrectionTarget] {

  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[CorrectionTarget] =
    parent.load(acquisitionMethod, confirmed).collect {
      case x: CorrectionTarget => x
    }

  override def libraries: Seq[AcquisitionMethod] = parent.libraries
}


@Component
@Profile(Array("carrot.targets.excel.annotation"))
class ExcelAnnotationLibraryAccess @Autowired()(parent: ExcelLibraryAccess) extends ReadonlyLibrary[AnnotationTarget] {

  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[AnnotationTarget] =
    parent.load(acquisitionMethod, confirmed).collect {
      case x: AnnotationTarget => x
    }

  override def libraries: Seq[AcquisitionMethod] = parent.libraries
}

@EnableConfigurationProperties
@Configuration
@ComponentScan
@Profile(Array("carrot.targets.excel.annotation", "carrot.targets.excel.correction"))
class ExcelLibraryAutoConfiguration extends Logging {
}

@ConfigurationProperties
class ExcelLibraryConfigurationProperties extends Logging {
  @Value("${carrot.targets.excel.filename}")
  val filename: String = ""

  @Value("${carrot.targets.excel.libname}")
  val libname: String = ""

  @Value("${carrot.targets.excel.instrument}")
  val instrument: String = ""

  @Value("${carrot.targets.excel.column}")
  val column: String = ""

  @Value("${carrot.targets.excel.ionMode}")
  private val mode: String = ""

  def ionMode: IonMode = mode match {
    case "negative" => NegativeMode()
    case "positive" => PositiveMode()
    case _ => PositiveMode()
  }

  override def toString: String = {
    new StringBuilder().append("{filename: ").append(filename)
        .append(", libname: ").append(libname)
        .append(", instrument: ").append(instrument)
        .append(", column: ").append(column)
        .append(", ionMode: ").append(ionMode.mode)
        .append("}").toString()
  }
}
