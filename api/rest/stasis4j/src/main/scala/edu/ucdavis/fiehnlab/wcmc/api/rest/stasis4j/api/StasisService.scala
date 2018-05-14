package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.springframework.http.HttpEntity

trait StasisService {
  def getTracking(sample: String): TrackingResponse

  def addTracking(sample: String, status: String): HttpEntity[TrackingResponse]

  def getResults(sample: String): ResultResponse

  def addResult(data: ResultData): HttpEntity[ResultData]

  def getAcquisition(sample: String): SampleResponse

  def createAcquisition(data: SampleData): HttpEntity[SampleData]

  def createAquisitionFromMinix(url: String): HttpEntity[SampleData]
}
