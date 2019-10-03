package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{AmazonSQSException, CreateQueueRequest, SendMessageRequest}
import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.google.gson.Gson
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Config
import org.apache.logging.log4j.scala.Logging
import org.springframework.context.ConfigurableApplicationContext

/**
  * publishes a message to SNS for later persistence
  */
class SQSCallbackHandler(queueName: String, config: Config) extends CallbackHandler(config) with Logging {

  val url: String = init()

  logger.info(s"established connection to queue: ${url}")
  /**
    * does something with the given data
    *
    * @param score
    * @param point
    * @param context
    */
  override def handle(objective: Objective[Point, Double], score: Double, point: Point, context: ConfigurableApplicationContext): Unit = {

    if (score.isNaN) {
      logger.error((s"recevied score was NAN, point = ${point} and score = ${score} for objective ${objective.getClass.getName}"))
    }
    else {
      point.hyperParamMap

      val content = Map(
        "parameters" -> point.hyperParamMap,
        "objective" -> objective.getClass.getName,
        "score" -> score,
        "config_id" -> config.hyperopt.identifier

      )
      logger.info(s"sending message -> ${point}")
      val request = new SendMessageRequest().withQueueUrl(url).withMessageBody(new Gson().toJson(content))
      AmazonSQSClientBuilder.defaultClient().sendMessage(request)
    }
  }

  def init(): String = {

    val sqs = AmazonSQSClientBuilder.defaultClient
    val create_request = new CreateQueueRequest(queueName).addAttributesEntry("DelaySeconds", "60").addAttributesEntry("MessageRetentionPeriod", "86400")

    try
      sqs.createQueue(create_request)
    catch {
      case e: AmazonSQSException =>
        if (!(e.getErrorCode == "QueueAlreadyExists")) throw e
    }
    AmazonSQSClientBuilder.defaultClient().getQueueUrl(queueName).getQueueUrl

  }
}
