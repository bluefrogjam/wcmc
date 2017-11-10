package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api

import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.FileMessage
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired

trait FileEventListener {
  @Autowired
  val queue: Queue = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  def recieveMessage(message: FileMessage): Unit
}
