package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl.rabbit

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.{Autowired, Value}
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
  override protected def doSubmit(task: Task): String = {
    rabbitTemplate.convertAndSend("carrot-tasks", task)
    "scheduled"
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


  @Value("${wcmc.pipeline.workflow.scheduler.queue:carrot-tasks}")
  val queueName: String = ""

  @Value("${wcmc.pipeline.workflow.scheduler.exchange:carrot-exchange}")
  val exchangeName: String = ""


  @Bean
  def queue: Queue = new Queue(queueName, false)

  @Bean
  def exchange = new FanoutExchange(exchangeName)

  @Bean
  def binding(queue: Queue, exchange: TopicExchange): Binding = BindingBuilder.bind(queue).to(exchange).`with`(queueName)

  @Bean
  def container(connectionFactory: ConnectionFactory, listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer
    container.setConnectionFactory(connectionFactory)
    container.setQueueNames(queueName)
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