package edu.ucdavis.fiehnlab.wcmc.utilities.metadata

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4J
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataRepository}
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.impl.MzXMLMetadataExtraction
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner


@RunWith(classOf[SpringRunner])
@SpringBootTest
class MetadataExtractionTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val metadataExtraction: MzXMLMetadataExtraction = null

  @Autowired
  val everything4j: Everything4J = null

  @Autowired
  val mdRepo: FileMetadataRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  def fixture = new {
    val filename = "Inj006_ExtrCtl_6.mzXML"
    val origFile: File = everything4j.loadAsFile(filename).get
  }


  "MetadataExtractor" must {
    "be defined" in {
      metadataExtraction should not be null
    }

    "get a FileMetadata object" in {
      val f = fixture
      logger.debug(s"size: ${f.origFile.length()}")

      val result = metadataExtraction.getMetadata(f.origFile)

      logger.debug(result.get.toString)

      result should not be empty
      result.get shouldBe a[FileMetadata]
      result.get.filename should be === f.filename
    }

  }

  "something" must {
    "store data in mongo" in {
      val f = fixture
      val filename = "Inj006_ExtrCtl_6.mzXML"

      val tmp = mdRepo.findByFilename(filename)
      mdRepo.delete(tmp)

      val fmd = metadataExtraction.getMetadata(f.origFile).get
      val results = mdRepo.findByFilename("Inj006_ExtrCtl_6.mzXML")

      results.manufacturer should be === "Agilent"
      results.model should be === "AGILENT TOF"
    }
  }

}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MetadataTestConfig
