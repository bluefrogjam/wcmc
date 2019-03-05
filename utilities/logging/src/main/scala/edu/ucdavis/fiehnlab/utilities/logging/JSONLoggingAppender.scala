package edu.ucdavis.fiehnlab.utilities.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.apache.logging.log4j.scala.Logging
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate

/**
  * required as a static reference, since logback loggers are by defaul
  * initialized wiht logback directly
  */
object JSONLoggingAppender {
  var mongoTemplate: MongoTemplate = null
}

class JSONLoggingAppender extends Logging {

  @Autowired
  var mongoTemplate: MongoTemplate = null

  @PostConstruct
  def init(): Unit = {
    JSONLoggingAppender.mongoTemplate = mongoTemplate
  }
}
