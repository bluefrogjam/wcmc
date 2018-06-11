package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

import java.util.Date

case class SampleData(
                         sample: String,
                         acquisition: Acquisition,
                         processing: Processing,
                         metadata: Metadata,
                         userdata: Userdata,
                         references: Array[Reference]
                     )

case class Acquisition(
                          instrument: String,
                          ionisation: String,
                          method: String
                      )

case class Processing(
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

case class Reference(
                        name: String,
                        value: String
                    )

case class SampleResponse(
                             id: String,
                             time: Date,
                             sample: String,
                             acquisition: Acquisition,
                             metadata: Metadata,
                             userdata: Userdata,
                             references: Array[Reference]
                         )

case class SampleMinix(
                          id: String,
                          url: String = "http://minix.fiehnlab.ucdavis.edu/rest/export",
                          minix: Boolean = true
                      )
