package edu.ucdavis.fiehnlab.utilities.logging

import javax.annotation.PostConstruct

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

/**
  * required as a static reference, since logback loggers are by defaul
  * initialized wiht logback directly
  */
object JSONLoggingAppender {
  var mongoTemplate: MongoTemplate = null
}

/**
  * should only be used for debugging and not production code! It is slow....,
  */
@Profile(Array("carrot.logging.json.enable"))
class JSONLoggingAppender extends AppenderBase[ILoggingEvent] with LazyLogging {

  @Autowired
  var mongoTemplate: MongoTemplate = null

  override def append(e: ILoggingEvent) = {

    if (JSONLoggingAppender.mongoTemplate != null) {
      val message = e.getMessage

      val index = message.indexOf("JSON:")
      if (index > 0) {

        try {
          val newMessage = message.substring(index + 5, message.length)
          JSONLoggingAppender.mongoTemplate.insert(newMessage, "carrot_logging")
        }
        catch {
          case e: Exception => logger.error(e.getMessage, e)
        }

      }
    }
    else {
      logger.trace("this logger has not been initialized yet!")
    }
  }


  @PostConstruct
  def init(): Unit = {
    JSONLoggingAppender.mongoTemplate = mongoTemplate

    logger.error("dropping existing collection...")
    mongoTemplate.dropCollection("carrot_logging")
    start()
  }
}
