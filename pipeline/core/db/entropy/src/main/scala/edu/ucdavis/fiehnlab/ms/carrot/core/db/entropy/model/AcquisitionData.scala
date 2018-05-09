package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model

case class AcquisitionData(
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
                       clazz: String,
                       species: String,
                       organ: String
                   )

case class Userdata(
                       label: String,
                       comment: String
                   )

case class AcquisitionResponse(
                                  statusCode: Int,
                                  body: AcquisitionData
                              )
