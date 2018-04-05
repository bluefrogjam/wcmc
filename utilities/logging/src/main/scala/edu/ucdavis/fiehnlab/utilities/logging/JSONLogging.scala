package edu.ucdavis.fiehnlab.utilities.logging

import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging
import org.springframework.context.annotation.{Bean, Configuration, Profile}

/**
  * simple diagnostics helper to simplify logging for us
  *
  */
object JSONLogging {

  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

}

/**
  * this trait needs to be extended by other mixins and allows us to easily logout information
  * in an JSON compatible format, which can be picked up by the @see JSONLoggingAppender
  */
trait JSONLogging extends LazyLogging {

  /**
    * based on mixins and other defined methods, this will log a json string to the console for debugging
    * as well as return
    */
  final def logJSON(map: Map[String, Any] = Map()): String = {
    if (supportsJSONLogging) {
      val message = JSONLogging.objectMapper.writeValueAsString(buildMessage() ++ map)
      logger.debug(s"isMatch JSON:${message}")
      message
    }
    else {
      ""
    }
  }

  /**
    * implementing class should override this class and call super on it
    *
    * @return
    */
  protected def buildMessage(): Map[String, Any] = Map("date" -> new Date())

  protected def supportsJSONLogging: Boolean = true
}


/**
  * logs settings and variables used for this log message
  */
trait JSONSettingsLogging extends JSONLogging {

  /**
    * references to all used settings
    */
  protected val usedSettings: Map[String, Any]

  override def buildMessage(): Map[String, Any] = super.buildMessage() + ("settings" -> usedSettings)
}

/**
  * defines a certain phase to be logged
  */
trait JSONPhaseLogging extends JSONLogging {

  /**
    * which phase we require to log
    */
  protected val phaseToLog: String

  override def buildMessage(): Map[String, Any] = super.buildMessage() + ("phase" -> phaseToLog)

}

/**
  * allows to log the given class and appends this information to the logging
  */
trait JSONAlgorithmLogging extends JSONLogging {

  /**
    * by default we want to log the actual implementation
    */
  protected val classUnderInvestigation: Any

  override def buildMessage(): Map[String, Any] = {
    super.buildMessage() + ("algorithm" -> classUnderInvestigation.getClass.getSimpleName)
  }

}

@Profile(Array("carrot.logging.json.enable"))
@Configuration
class JSONLoggingConfiguration {

  @Bean
  def jsonLoggingAppender(): JSONLoggingAppender = new JSONLoggingAppender()
}