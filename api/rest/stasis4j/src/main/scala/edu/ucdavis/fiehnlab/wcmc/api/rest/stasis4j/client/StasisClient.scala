package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api._
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpEntity, HttpMethod, ResponseEntity}
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConverters._

@Component
class StasisClient extends StasisService with LazyLogging {
  @Autowired
  private val restTemplate: RestTemplate = null

  @Value("${stasis.baseurl}")
  val baseUrl = ""

  @PostConstruct
  def post = {
    logger.info(s"utilizing url: $baseUrl")
  }

  private val trackingPath = "tracking"
  private val resultPath = "result"
  private val acquisitionPath = "acquisition"

  override def getTracking(sample: String): TrackingResponse = restTemplate.getForObject(s"${baseUrl}/${trackingPath}/${sample}", classOf[TrackingResponse])

  override def addTracking(data: TrackingData): ResponseEntity[TrackingResponse] = {
    logger.info(s"Adding tracking: ${data}")
    restTemplate.postForEntity(s"${baseUrl}/${trackingPath}", data, classOf[TrackingResponse])
  }

  override def getResults(sample: String): ResultResponse = {
    if (sample.contains('.')) {
      logger.warn("The sample name provided might contain an extension. Please provide sample name without extension!")
    }
    val result = restTemplate.getForEntity[ResultResponse](s"${baseUrl}/${resultPath}/${sample}", classOf[ResultResponse])

    if (result.getStatusCodeValue == 200)
      result.getBody
    else
      ResultResponse("none", "none", new Date(), Map[String, Seq[Injection]]().asJava)
  }

  override def addResult(data: ResultData): ResponseEntity[ResultData] = restTemplate.postForEntity(s"${baseUrl}/${resultPath}", data, classOf[ResultData])

  override def getAcquisition(sample: String): SampleResponse = restTemplate.getForObject(s"${baseUrl}/${acquisitionPath}/${sample}", classOf[SampleResponse])

  override def createAcquisition(data: SampleData): ResponseEntity[SampleData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", data, classOf[SampleData])

  override def deleteTracking(sample: String): HttpEntity[String] = restTemplate.execute[ResponseEntity[String]](s"${baseUrl}/${trackingPath}/${sample}", HttpMethod.DELETE, null, null)

  override def schedule(sample: String, method: String, mode: String, env: String): ResponseEntity[String] = restTemplate.postForEntity(s"${baseUrl}/schedule", ScheduleData(sample, method, mode, env, "3"), classOf[String])

}
