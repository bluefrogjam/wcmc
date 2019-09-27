package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.net.URI
import java.util.Date

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api._
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, ResponseEntity}
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConverters._

@Profile(Array("!carrot.nostasis"))
@Component
class StasisClient extends StasisService with Logging {
  @Autowired
  private val restTemplate: RestTemplate = null

  @Value("${stasis.baseurl}")
  val baseUrl = ""

  @Value("${stasis.key:#{environment.STASIS_KEY}}")
  val apiKey = ""

  @PostConstruct
  def post = {
    logger.info(s"utilizing url: $baseUrl")

    if (apiKey == "") {
      throw new Exception("please specify an api key!")
    }

  }

  private def headers(): HttpHeaders = {
    val h = new HttpHeaders()
    h.add("x-api-key", this.apiKey)
    h
  }

  private val trackingPath = "tracking"
  private val resultPath = "result"
  private val acquisitionPath = "acquisition"

  override def getTracking(sample: String): TrackingResponse = restTemplate.exchange(new URI(s"${baseUrl}/${trackingPath}/${sample}"), HttpMethod.GET, new HttpEntity("", headers()), classOf[TrackingResponse]).getBody()

  override def addTracking(data: TrackingData): ResponseEntity[TrackingResponse] = {
    logger.info(s"Adding tracking: ${data}")
    restTemplate.postForEntity(s"${baseUrl}/${trackingPath}", new HttpEntity(data, headers()), classOf[TrackingResponse])
  }

  override def getResults(sample: String): ResultResponse = {
    if (sample.contains('.')) {
      logger.warn("The sample name provided might contain an extension. Please provide sample name without extension!")
    }
    val result = restTemplate.exchange(s"${baseUrl}/${resultPath}/${sample}", HttpMethod.GET, new HttpEntity("", headers()), classOf[ResultResponse])

    if (result.getStatusCodeValue == 200)
      result.getBody
    else
      ResultResponse("none", "none", new Date(), Map[String, Seq[Injection]]().asJava)
  }

  override def addResult(data: ResultData): ResponseEntity[ResultData] = restTemplate.postForEntity(s"${baseUrl}/${resultPath}", new HttpEntity(data, headers()), classOf[ResultData])

  override def getAcquisition(sample: String): SampleResponse = restTemplate.exchange(s"${baseUrl}/${acquisitionPath}/${sample}", HttpMethod.GET, new HttpEntity("", headers()), classOf[SampleResponse]).getBody

  override def createAcquisition(data: SampleData): ResponseEntity[SampleData] = restTemplate.postForEntity(s"${baseUrl}/${acquisitionPath}", new HttpEntity(data, headers()), classOf[SampleData])

  override def deleteTracking(sample: String): HttpEntity[String] = restTemplate.exchange(s"${baseUrl}/${trackingPath}/${sample}", HttpMethod.DELETE, new HttpEntity("", headers()), classOf[String])

  override def schedule(sample: String, method: String, mode: String, env: String): ResponseEntity[ScheduleData] = restTemplate.postForEntity(s"${baseUrl}/schedule", new HttpEntity(ScheduleData(sample, method, mode, env, "86"), headers()), classOf[ScheduleData])

}

@Component
@Profile(Array("carrot.nostasis"))
class NoOpStasisService() extends StasisService {
  val id = System.currentTimeMillis().toString

  override def getTracking(sample: String): TrackingResponse = TrackingResponse(id, sample, Seq.empty)

  override def addTracking(data: TrackingData): ResponseEntity[TrackingResponse] = ResponseEntity.ok(TrackingResponse("", "", Seq.empty))

  override def getResults(sample: String): ResultResponse = ResultResponse(id, sample, new Date(), new java.util.HashMap())

  override def addResult(data: ResultData): ResponseEntity[ResultData] = ResponseEntity.ok(data)

  override def getAcquisition(sample: String): SampleResponse = SampleResponse(id, new Date(), sample, Acquisition("", "", ""), Metadata("", "", ""), Userdata("", ""), Array.empty)

  override def createAcquisition(data: SampleData): ResponseEntity[SampleData] = ResponseEntity.ok(data)

  override def deleteTracking(sample: String): HttpEntity[String] = ResponseEntity.ok(sample)

  override def schedule(sample: String, method: String, mode: String, env: String): ResponseEntity[ScheduleData] = ResponseEntity.ok(ScheduleData(sample, method, mode, env, "0"))
}
