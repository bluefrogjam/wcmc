package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("jenny"))
class JennyRunner extends WordSpec with LazyLogging with ShouldMatchers {
  @Autowired
  val taskRunner: TaskRunner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Run samples" should {
    "process samples" ignore {
      //      val fileList = Source.fromFile("/g/study-jenny/aws-tracking.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
      //      val fileList = Source.fromFile("/g/study-jenny/small-jenny-trouble.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
      //      val fileList = Seq("BioRec_LipidsPos_PhIV_024.mzml").par
      val fileList = Source.fromFile("/g/study-jenny/clean to process/files-002.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
      logger.info(s"loaded ${fileList.size} filenames")

      val forkJoinPool = new ForkJoinPool(10)
      fileList.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

      val results = Map("success" -> Seq.empty[String], "failed" -> Seq.empty[String])

      logger.info("Running...")
      fileList.foreach { sample =>
        logger.info(sample)
        val task = Task(s"${sample} processing",
          "linuxmant@gmail.com",
          AcquisitionMethod(
            ChromatographicMethod("jenny-tribe", Some("6530"), Some("test"), Some(PositiveMode()))
          ),
          Seq(SampleToProcess(sample, "", "", sample,
            Matrix(System.currentTimeMillis().toString, "human", "plasma", Seq.empty)
          )),
          mode = "lcms",
          env = "prod"
        )

        val start = System.currentTimeMillis()
        try {
          taskRunner.run(task)
          logger.info(s"\tSuccessfully finished processing ${sample}")
          results("success") ++ s"${sample} - ${System.currentTimeMillis() - start}ms"
        } catch {
          case ex: Exception =>
            logger.error(s"\tFailed processing ${sample}")
            results("failed") ++ s"${sample} - ${System.currentTimeMillis() - start}ms"
        }
      }
    }
  }
}
