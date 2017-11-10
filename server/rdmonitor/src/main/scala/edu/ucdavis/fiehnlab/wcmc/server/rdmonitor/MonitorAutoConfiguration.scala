package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
@EnableRabbit
class MonitorAutoConfiguration {
  @Value("${wcmc.monitor.rabbit.queue:monitor-newFile-queue}")
  val queueName: String = ""

  @Bean
  def monitor: Monitor = new Monitor()

  @Bean
  def queue = new Queue(queueName)

  @Bean
  def container(connectionFactory: ConnectionFactory, listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer = {
    new RabbitAdmin(connectionFactory).declareQueue(queue)
    val container = new SimpleMessageListenerContainer
    container.setConnectionFactory(connectionFactory)
    container.setQueueNames(queueName)
    container.setMessageListener(listenerAdapter)
    container
  }

}
