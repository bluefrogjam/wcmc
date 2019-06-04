package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.springframework.http.{HttpEntity, ResponseEntity}

trait StasisService {
  def getTracking(sample: String): TrackingResponse

  def addTracking(data: TrackingData): HttpEntity[TrackingResponse]

  def getResults(sample: String): ResultResponse

  def addResult(data: ResultData): ResponseEntity[ResultData]

  def getAcquisition(sample: String): SampleResponse

  def createAcquisition(data: SampleData): HttpEntity[SampleData]

  def deleteTracking(sample: String): HttpEntity[String]

  def schedule(sample: String, method: String, mode: String, env: String): HttpEntity[ScheduleData]
}
