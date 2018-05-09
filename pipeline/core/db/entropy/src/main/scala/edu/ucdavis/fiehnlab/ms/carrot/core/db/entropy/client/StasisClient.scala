package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.client

import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.api.StasisService
import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class StasisClient extends StasisService {
  @Autowired
  val restTemplate: RestTemplate = null

  @Value("${stasis.evironment:dev-}")
  val env: String = ""

  private val baseUrl = s"http://${env}api.metabolomics.us/stasis"
  private val trackingPath = "/tracking"
  private val resultPath = "/result"
  private val acquisitionPath = "/acquisition"

  override def getTracking(sample: String): TrackingData = restTemplate.getForEntity(s"${baseUrl}/${resultPath}/${sample}", classOf[TrackingData]).getBody

  override def addTracking(sample: String, status: String): TrackingResponse = restTemplate.postForObject(s"${baseUrl}/${acquisitionPath}", createRequestEntity[TrackingData](TrackingData(sample, status)), classOf[TrackingResponse])

  override def getResults(sample: String): ResultData = restTemplate.getForObject(s"${baseUrl}/${resultPath}/${sample}", classOf[ResultData])

  override def addResult(data: ResultData): ResultResponse = restTemplate.postForObject(s"${baseUrl}/${acquisitionPath}", createRequestEntity[ResultData](data), classOf[ResultResponse])

  override def getAcquisition(sample: String): AcquisitionData = restTemplate.getForObject(s"${baseUrl}/${acquisitionPath}/${sample}", classOf[AcquisitionData])

  override def createAcquisition(data: AcquisitionData): AcquisitionResponse = restTemplate.postForObject(s"${baseUrl}/${acquisitionPath}", createRequestEntity[AcquisitionData](data), classOf[AcquisitionResponse])

  override def createAquisitionFromMinix(minixid: AcquisitionData): AcquisitionResponse = ???


  /**
    * Builds the HttpEntity with data to be used in restTemplate request
    *
    * @param data
    * @tparam T
    * @return
    */
  private def createRequestEntity[T](data: T): HttpEntity[LinkedMultiValueMap[String, T]] = {
    val headers: HttpHeaders = new HttpHeaders()
    headers.setContentType(MediaType.APPLICATION_JSON)

    val params: LinkedMultiValueMap[String, T] = new LinkedMultiValueMap[String, T]()
    params.add("body", data)

    new HttpEntity(params, headers)
  }
}
