package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

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

case class SampleMinix(
                          id: String,
                          url: String = "http://minix.fiehnlab.ucdavis.edu/rest/export",
                          minix: Boolean = true
                      )
