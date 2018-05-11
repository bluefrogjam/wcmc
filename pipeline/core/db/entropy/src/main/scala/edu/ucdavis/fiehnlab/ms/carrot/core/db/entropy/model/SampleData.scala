package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model

import java.util.Date

case class SampleData(
                         sample: String,
                         acquisition: Acquisition,
                         metadata: Metadata,
                         userdata: Userdata
                     )

case class Acquisition(
                          instrument: String,
                          name: String,
                          ionisation: String,
                          method: String
                      )

case class Metadata(
                       `class`: String,
                       species: String,
                       organ: String
                   )

case class Userdata(
                       label: String,
                       comment: String
                   )

case class SampleResponse(
                             sample: String,
                             acquisition: Acquisition,
                             metadata: Metadata,
                             userdata: Userdata,
                             id: String,
                             time: Date
                         )

case class SampleMinix(url: String, minix: Boolean = true)
