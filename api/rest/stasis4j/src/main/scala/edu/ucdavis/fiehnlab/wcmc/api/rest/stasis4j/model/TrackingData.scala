package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

import java.util.Date

case class TrackingData(
                           sample: String,
                           status: String,
                           fileHandle: String
                       )

case class TrackingResponse(
                               id: String,
                               sample: String,
                               status: Seq[StatusItem]
                           )

case class StatusItem(
                         time: Date,
                         priority: Int,
                         fileHandle: String,
                         value: String
                     )
