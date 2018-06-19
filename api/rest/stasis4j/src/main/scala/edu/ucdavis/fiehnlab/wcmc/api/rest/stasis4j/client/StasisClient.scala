package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api._
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

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

  override def addTracking(data: TrackingData): ResponseEntity[TrackingResponse] = restTemplate.postForEntity(s"${baseUrl}/${trackingPath}", data, classOf[TrackingResponse])

  override def getResults(sample: String): ResultResponse = restTemplate.getForObject(s"${baseUrl}/${resultPath}/${sample}", classOf[ResultResponse])

  override def addResult(data: ResultData): ResponseEntity[ResultData] = restTemplate.postForEntity(s"${baseUrl}/${resultPath}", data, classOf[ResultData])

  override def getAcquisition(sample: String): SampleResponse = restTemplate.getForObject(s"${baseUrl}/${acquisitionPath}/${sample}", classOf[SampleResponse])

  override def createAcquisition(data: SampleData): ResponseEntity[SampleData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", data, classOf[SampleData])
}
