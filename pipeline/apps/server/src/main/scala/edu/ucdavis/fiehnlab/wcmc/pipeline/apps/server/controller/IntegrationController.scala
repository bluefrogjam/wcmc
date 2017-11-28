package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import java.net.{URI, URL}

import edu.ucdavis.fiehnlab.utilities.minix.SXStudyFileReader
import edu.ucdavis.fiehnlab.utilities.minix.types.SampleInformationResult
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.web.bind.annotation._

@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/integration"))
class IntegrationController {

  @Autowired
  val sXStudyFileReader: SXStudyFileReader = null

  @Value("${minix.server.web:http://minix.fiehnlab.ucdavis.edu}")
  val minixUrl: String = null

  /**
    * fetches a minix studie from the system
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/minix/{id}"), method = Array(RequestMethod.GET))
  def fetchMinixStudie(@PathVariable("id") id: Long): java.util.List[SampleInformationResult] = {
    val url = new URI(s"${minixUrl}/rest/export/${id}").toURL
    val stream = url.openStream()

    try {
      sXStudyFileReader.loadData(stream)
    }
    finally {
      stream.close()
    }
  }
}
