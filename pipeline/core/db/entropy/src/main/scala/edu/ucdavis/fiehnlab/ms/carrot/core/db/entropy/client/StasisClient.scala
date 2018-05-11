package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.client

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.api.StasisService
import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class StasisClient extends StasisService with LazyLogging {
  @Autowired
  val restTemplate: RestTemplate = null

  @Value("${stasis.baseurl:https://dev-api.metabolomics.us/stasis}")
  val baseUrl = ""

  private val trackingPath = "tracking"
  private val resultPath = "result"
  private val acquisitionPath = "acquisition"

  override def getTracking(sample: String): TrackingResponse = restTemplate.getForObject(s"${baseUrl}/${trackingPath}/${sample}", classOf[TrackingResponse])

  override def addTracking(sample: String, status: String): ResponseEntity[TrackingData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", TrackingData(sample, status), classOf[TrackingData])

  override def getResults(sample: String): ResultResponse = restTemplate.getForObject(s"${baseUrl}/${resultPath}/${sample}", classOf[ResultResponse])

  override def addResult(data: ResultData): ResponseEntity[ResultData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", data, classOf[ResultData])

  override def getAcquisition(sample: String): SampleResponse = restTemplate.getForObject(s"${baseUrl}/${acquisitionPath}/${sample}", classOf[SampleResponse])

  override def createAcquisition(data: SampleData): ResponseEntity[SampleData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", data, classOf[SampleData])

  override def createAquisitionFromMinix(url: String): ResponseEntity[SampleData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", SampleMinix(url), classOf[SampleData])

}
