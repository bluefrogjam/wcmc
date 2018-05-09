package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model

case class TrackingData(
                           sample: String,
                           status: String
                       )

case class TrackingResponse(
                               statusCode: Int,
                               body: TrackingData
                           )
