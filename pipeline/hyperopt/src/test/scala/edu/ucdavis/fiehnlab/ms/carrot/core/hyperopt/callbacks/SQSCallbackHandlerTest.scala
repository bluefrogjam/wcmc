package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks

import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.{Config, Hyperopt, Stages}
import org.scalatest.WordSpec

class SQSCallbackHandlerTest extends WordSpec {

  "SQSCallbackHandlerTest" should {
    val client = AmazonSQSAsyncClientBuilder.defaultClient()
    val callback = new SQSCallbackHandler("CarrotHyperoptQueue-test", Config(Hyperopt( List.empty, List.empty, "", Stages())))
    "init" in {

      callback.init()
    }

    "handle" in {

      callback.handle(
        objective = new Objective[Point, Double] {
          override def apply(point: Point): Double = 0
        },
        score = 0,
        point = new Point(hyperParamMap = Map("test" -> 1.0)),
        context = null
      )
    }

    "must be able to receive a message afterwards" in {

      assert(client.receiveMessage(callback.url).getMessages.size() > 0)
    }

  }
}
