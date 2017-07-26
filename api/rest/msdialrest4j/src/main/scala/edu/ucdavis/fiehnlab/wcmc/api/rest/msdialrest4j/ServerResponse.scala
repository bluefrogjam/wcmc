package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

/**
  * exspected server response
  *
  * @param filename
  * @param link
  * @param message
  * @param error
  */
case class ServerResponse(filename: String, link: String, message: String, error: String)
