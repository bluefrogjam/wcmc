package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.diagnostics

import javax.annotation.PostConstruct

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
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
@Component
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
      logger.warn("sorry no mongo template set!")
    }
  }


  @PostConstruct
  def init(): Unit = {
    JSONLoggingAppender.mongoTemplate = mongoTemplate

    logger.warn("dropping existing collection...")
    mongoTemplate.dropCollection("carrot_logging")
    start()
  }
}
