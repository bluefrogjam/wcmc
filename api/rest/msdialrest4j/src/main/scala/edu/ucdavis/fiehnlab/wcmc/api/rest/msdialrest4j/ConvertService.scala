package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.{DataFormerClient, FileType}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ConvertService extends LazyLogging {
  @Autowired
  val dfClient: DataFormerClient = null

  /**
    * Converts a raw data file to abf
    *
    * @param input a raw file (.d.zip; .raw; [wiff].zip)
    * @return an abf file
    */
  def getAbfFile(input: File): Option[File] = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)


    //upload
    var result = mapper.readValue(dfClient.upload(input), classOf[UploadResponse])

    //convert
    if (!result.abf.equals("ok")) {
      None
    } else {
      val cr = dfClient.convert(input.getName)
      if (cr.getOrElse("abf", "").isEmpty) {
        None
      }

      //download
      Option(dfClient.download(input, FileType.ABF))
    }
  }
}


case class UploadResponse(filename: String, abf: String, mzml: String)

case class ConversionResponse(filename: String, abf: String, mzml: String)
