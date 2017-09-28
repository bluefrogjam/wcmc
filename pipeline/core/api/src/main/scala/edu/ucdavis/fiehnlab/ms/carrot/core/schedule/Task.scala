package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod

/**
  * This defines a basic task to be submitted to the carrot system
  * for processing and calculations
  */
case class Task(

                 /**
                   * the name of the task
                   */
                 name: String,

                 /**
                   * the email of the submitter
                   */
                 email: String,

                 /**
                   * the exact acquisition method we would like to use
                   * with this task
                   */
                 acquisitionMethod: AcquisitionMethod,

                 /**
                   * defines a list of samples to process
                   */
                 samples: Seq[SampleToProcess]

               )








