package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl.rabbit

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}


/**
  * schedules carrot based tasks to a local queue
  */
class RabbitTaskScheduler extends TaskScheduler {

  @Autowired
  private val rabbitTemplate: RabbitTemplate = null

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  override protected def doSubmit(task: Task) = {
    rabbitTemplate.convertAndSend("carrot-tasks", task)
  }
}

/**
  * listens to the the defined rabbitmq queue and processing
  */
class RabbitTaskRunner {

  @Autowired
  val taskRunner: TaskRunner = null

  @RabbitListener(queues = Array("carrot-tasks"))
  def receive(message: Task) = {
    taskRunner.run(message)
  }
}

@Configuration
class RabbitTaskAutoconfiguration {

  @Bean
  def queue: Queue = new Queue("carrot-tasks", false)

  @Bean
  def exchange = new FanoutExchange("carrot-test-exchange")

  @Bean
  def binding(queue: Queue, exchange: TopicExchange): Binding = BindingBuilder.bind(queue).to(exchange).`with`("carrot-tasks")

  @Bean def container(connectionFactory: Nothing, listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer
    container.setConnectionFactory(connectionFactory)
    container.setQueueNames("carrot-tasks")
    container.setMessageListener(listenerAdapter)
    container
  }

  @Bean
  def rabbitTaskRunner: RabbitTaskRunner = {
    new RabbitTaskRunner
  }

  @Bean
  def taskScheduler: TaskScheduler = new RabbitTaskScheduler
}