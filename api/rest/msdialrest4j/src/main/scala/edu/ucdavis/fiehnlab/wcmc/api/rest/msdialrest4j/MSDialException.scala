package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import org.springframework.http.ResponseEntity

/**
  * an msdial exception if something goes wrong
  *
  * @param result
  */
class MSDialException(result: ResponseEntity[ServerResponse]) extends Exception
