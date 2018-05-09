package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.api

import edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model._

trait StasisService {
  def getTracking(sample: String): TrackingData

  def addTracking(sample: String, status: String): TrackingResponse

  def getResults(sample: String): ResultData

  def addResult(data: ResultData): ResultResponse

  def getAcquisition(sample: String): AcquisitionData

  def createAcquisition(data: AcquisitionData): AcquisitionResponse

  def createAquisitionFromMinix(minixid: AcquisitionData): AcquisitionResponse
}
