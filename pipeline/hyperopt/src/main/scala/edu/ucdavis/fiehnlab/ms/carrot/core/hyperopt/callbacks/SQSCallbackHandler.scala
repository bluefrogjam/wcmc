package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks

import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClient}
import com.amazonaws.services.sns.model.{CreateTopicRequest, PublishRequest}
import com.amazonaws.services.sqs.AmazonSQS
import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.google.gson.Gson
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Config
import org.springframework.context.ConfigurableApplicationContext
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{AmazonSQSException, CreateQueueRequest, SendMessageRequest}
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.AmazonSQSException

/**
  * publishes a message to SNS for later persistence
  */
class SQSCallbackHandler(queueName: String, sqsClient: AmazonSQS, config: Config) extends CallbackHandler(config) {

  val url: String = init()

  /**
    * does something with the given data
    *
    * @param score
    * @param point
    * @param context
    */
  override def handle(objective: Objective[Point, Double], score: Double, point: Point, context: ConfigurableApplicationContext): Unit = {

    point.hyperParamMap

    val content = Map(
      "parameters" -> point.hyperParamMap,
      "objective" -> objective.getClass.getName,
      "score" -> score,
      "config_id" -> config.hyperopt.identifier

    )
    val request = new SendMessageRequest().withQueueUrl(url).withMessageBody(new Gson().toJson(content))
    sqsClient.sendMessage(request)
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
    sqsClient.getQueueUrl(queueName).getQueueUrl

  }
}
