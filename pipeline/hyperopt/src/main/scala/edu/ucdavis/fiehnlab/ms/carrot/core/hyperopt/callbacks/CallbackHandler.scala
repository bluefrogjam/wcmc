package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import org.springframework.context.ConfigurableApplicationContext

/**
  * simple class to support callbacks in the hyperopt objective to track results in external databases or so
  */
trait CallbackHandler {

  /**
    * does something with the given data
    *
    * @param score
    * @param point
    * @param context
    */
  def handle(objective: Objective[Point, Double], score: Double, point: Point, context: ConfigurableApplicationContext)
}
