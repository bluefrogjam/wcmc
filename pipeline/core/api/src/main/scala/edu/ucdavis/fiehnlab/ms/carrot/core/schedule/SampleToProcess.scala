package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.Matrix

/**
  * a basic sample which should be processed
  *
  * @param fileName
  * @param matrix
  */
case class SampleToProcess(fileName: String, className: String, comment: String, label: String, matrix: Matrix)
