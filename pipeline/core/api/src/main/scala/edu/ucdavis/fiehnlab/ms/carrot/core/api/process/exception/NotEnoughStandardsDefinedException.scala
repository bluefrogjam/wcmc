package edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception

/**
  * is thrown when there were not enough standards defined for this correction model
  *
  * @param message
  */
class NotEnoughStandardsDefinedException(override val message: String) extends RetentionIndexCorrectionException(message)

/**
  * is thrown when there are not enough standards found for this correction model
  * @param message
  */
class NotEnoughStandardsFoundException(override val message: String) extends RetentionIndexCorrectionException(message)

/**
  * is thrown when the standards are not in order
  * @param message
  */
class StandardsNotInOrderException(override val message: String) extends RetentionIndexCorrectionException(message)

/**
  * a required standard was not found
  * @param message
  */
class RequiredStandardNotFoundException(override val message: String) extends RetentionIndexCorrectionException(message)

/**
  * is thrown, when a standard is annotated twice
  * @param message
  */
class StandardAnnotatedTwice(override val message: String) extends RetentionIndexCorrectionException(message)

/**
  * it's thrown when the rentetion index correction fails
  * @param message
  */
class RetentionIndexCorrectionException(val message: String) extends Exception(message)
