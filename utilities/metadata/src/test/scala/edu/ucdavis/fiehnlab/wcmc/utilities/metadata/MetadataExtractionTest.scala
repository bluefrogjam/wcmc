package edu.ucdavis.fiehnlab.wcmc.utilities.metadata

import java.io.File

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4J
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataRepository}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.eclipse", "carrot.metadata.mongo"))
class MetadataExtractionTest extends WordSpec with Matchers with Logging {

  @Autowired
  val metadataExtraction: MetadataExtraction = null

  @Autowired
  val everything4j: Everything4J = null

  @Autowired
  val mdRepo: FileMetadataRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  def fixture = new {
    val filename = "Inj006_ExtrCtl_6.mzXML"
    val origFile: File = everything4j.loadAsFile(filename).get
  }


  "MetadataExtractor" ignore {
    "be defined" in {
      metadataExtraction should not be null
    }

    "get a FileMetadata object" in {
      val f = fixture

      val result = metadataExtraction.getMetadata(f.origFile)

      result should not be empty
      result.get shouldBe a[FileMetadata]
      result.get.filename should be === f.filename
    }

  }

  "something" ignore {
    "store data in mongo" in {
      val f = fixture
      val filename = "Inj006_ExtrCtl_6.mzXML"

      mdRepo.deleteAll()

      val fmd = metadataExtraction.getMetadata(f.origFile).get
      val results = mdRepo.findByFilename("Inj006_ExtrCtl_6.mzXML")

      results.manufacturer should be === "Agilent"
      results.model should be === "AGILENT TOF"
    }
  }

}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MetadataTestConfig
