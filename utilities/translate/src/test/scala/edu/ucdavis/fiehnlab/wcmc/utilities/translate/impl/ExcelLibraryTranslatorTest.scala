package edu.ucdavis.fiehnlab.wcmc.utilities.translate.impl

import java.io.File

import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.excel._
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.reflect.io.Path

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.translate.excel", "carrot.targets.excel.correction", "carrot.targets.excel.annotation"))
class ExcelLibraryTranslatorTest extends WordSpec with Matchers with Logging {

  @Autowired
  val translator: ExcelLibraryTranslator = null

  @Autowired
  val excelLib: ExcelLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ExcelLibraryTranslatorTest" should {

    "convert an excel file" in {
      val method = AcquisitionMethod(ChromatographicMethod("test-pos", Some("test"), Some("test"), Some(PositiveMode())))

      translator.convertToFile(excelLib, method)

      val outfilename = s"${method.chromatographicMethod.name}_${method.chromatographicMethod.ionMode.get.mode.substring(0, 3)}.yaml"
      Path(outfilename).exists shouldBe true
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class ExcelTranslatorTestConfiguration {
  @Bean
  def storage: ResourceStorage = new ResourceStorage {
    override def store(file: File): Unit = {
      val curDir = System.getProperty("user.dir")
    }

    override def delete(name: String): Unit = {}
  }

  @Bean
  def excelProps: ExcelLibraryConfigurationProperties = new ExcelLibraryConfigurationProperties()

  @Bean
  def loader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("../../pipeline/core/db/excel/src/test"))

  @Bean
  def excelLib: ExcelLibraryAccess = new ExcelLibraryAccess()

}
