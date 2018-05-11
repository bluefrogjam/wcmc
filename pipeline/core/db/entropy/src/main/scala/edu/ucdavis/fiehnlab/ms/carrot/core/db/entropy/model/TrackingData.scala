package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model

import java.util.Date

case class TrackingData(
                           sample: String,
                           status: String
                       )

case class TrackingResponse(
                               id: String,
                               sample: String,
                               status: Array[StatusItem]
                           )

case class StatusItem(
                         value: String,
                         time: Date
                     )
