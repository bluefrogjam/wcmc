package edu.ucdavis.fiehnlab.ms.carrot.core.api.action

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
  * defines a workflow related action
  * Created by wohlgemuth on 7/12/17.
  */
trait Action {

	@Autowired
	protected val applicationContext:ApplicationContext = null

	/**
		* Defines the priority for this action when multiple actions can run
		*/
	val priority: Int = 0

  /**
    * executes this action
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  def run(sample: Sample,experimentClass: ExperimentClass,experiment:Experiment)
}

/**
  * actions which are supposed to run after the annotation process
  */
trait PostAction extends Action

//only for java code ease of mind
abstract class PostActionWrapper extends PostAction
