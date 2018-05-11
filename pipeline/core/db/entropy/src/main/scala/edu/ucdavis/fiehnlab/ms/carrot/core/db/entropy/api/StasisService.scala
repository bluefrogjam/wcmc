package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.api

import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model._
import org.springframework.http.HttpEntity

trait StasisService {
  def getTracking(sample: String): TrackingResponse

  def addTracking(sample: String, status: String): HttpEntity[TrackingData]

  def getResults(sample: String): ResultResponse

  def addResult(data: ResultData): HttpEntity[ResultData]

  def getAcquisition(sample: String): SampleResponse

  def createAcquisition(data: SampleData): HttpEntity[SampleData]

  def createAquisitionFromMinix(url: String): HttpEntity[SampleData]
}
