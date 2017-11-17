package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl.rabbit

import java.io.ByteArrayOutputStream

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.AbstractMessageConverter
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, Configuration}

import scala.reflect.classTag


/**
  * schedules carrot based tasks to a local queue
  */
class RabbitTaskScheduler extends TaskScheduler {

  @Value("${wcmc.pipeline.workflow.scheduler.queue:carrot-tasks}")
  val queueName: String = ""

  @Autowired
  private val rabbitTemplate: RabbitTemplate = null

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  override protected def doSubmit(task: Task): String = {
    rabbitTemplate.convertAndSend(queueName, task)
    "scheduled"
  }
}

/**
  * listens to the the defined rabbitmq queue and processing
  */
class RabbitTaskRunner extends MessageListener {

  @Autowired
  val objectMapper: ObjectMapper = null

  @Autowired
  val taskRunner: TaskRunner = null

  val logger:Logger = LoggerFactory.getLogger(getClass.getName)

  /**
    * converts messages for us from json to our actual content
    *
    * @param message
    */
  override final def onMessage(message: Message): Unit = {
    val content: Task = objectMapper.readValue(message.getBody, classTag[Task].runtimeClass).asInstanceOf[Task]
    logger.info(s"received new task to process: ${content}")
    taskRunner.run(content)
    logger.info(s"processing of task is done, ${content}!")
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
  def binding(queue: Queue, exchange: FanoutExchange): Binding = BindingBuilder.bind(queue).to(exchange)

  @Bean
  def container(connectionFactory: ConnectionFactory): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer
    container.setConnectionFactory(connectionFactory)
    container.setQueues(queue)
    container.setMessageListener(rabbitTaskRunner)
    container.setMessageConverter(messageConverter)

    container
  }

  @Bean
  def rabbitTaskRunner: RabbitTaskRunner = {
    new RabbitTaskRunner
  }

  @Bean
  def messageConverter: MessageConverter = new MessageConverter

  @Bean
  def scheduler: TaskScheduler = new RabbitTaskScheduler
}

class MessageConverter extends AbstractMessageConverter {

  @Autowired
  val objectMapper: ObjectMapper = null

  override def fromMessage(message: Message): AnyRef = {
    objectMapper.readValue(message.getBody, classOf[Any]).asInstanceOf[AnyRef]
  }

  override def createMessage(content: scala.Any, messageProperties: MessageProperties): Message = {

    val stream = new ByteArrayOutputStream()
    objectMapper.writeValue(stream, content)

    new Message(stream.toByteArray, new MessageProperties)
  }
}
