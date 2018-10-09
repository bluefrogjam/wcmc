package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("jenny"))
class JennyRunner extends WordSpec with LazyLogging with ShouldMatchers {
  @Autowired
  val taskRunner: TaskRunner = null

  @Autowired
  val stasis: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Run samples" should {
    //      val fileList = Source.fromFile("/g/study-jenny/aws-tracking.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
    //      val fileList = Source.fromFile("/g/study-jenny/small-jenny-trouble.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
    //      val fileList = Seq("BioRec_LipidsPos_PhIV_024.mzml").par
    val fileList = Source.fromFile("/g/study-jenny/processed/batch1/batch-1b-pos-miss-run2.txt").getLines().filterNot(_.isEmpty).map(_ + ".mzml").toSeq.par
    logger.info(s"loaded ${fileList.size} filenames")
    "process samples bulk" ignore {

      val forkJoinPool = new ForkJoinPool(5)
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

    "process single file n times" in {
      fileList.foreach { sample =>
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

        try {
          val start = System.currentTimeMillis()
          taskRunner.run(task)
          println()
          logger.info(s"\tSuccessfully finished processing ${sample} in ${(System.currentTimeMillis() - start) / 1000} s")
          println()

        } catch {
          case ex: Exception =>
            logger.error(s"\tFailed processing ${sample}.", ex)
        }
      }
    }
  }

  def save_sample(idx: Int, sample: String): Unit = {
    val results = stasis.getResults(sample.replaceAll(".mzml", ""))

    val pw = new PrintWriter(new File(s"/g/study-jenny/biorecs_reprocess/${sample}-${idx}.csv"))
    pw.println(s"T-id,T-mass,T-RI,A-intensity,A-replaced,A-RI,A-orig_RT,rt Distance,A-mass,A-massError,A-massErrorPPM")
    results.injections.asScala.foreach(item => item._2.foreach(injection => {
      injection.results.foreach(ir => {
        pw.println(s"${ir.target.id},${ir.target.mass},${ir.target.retentionIndex},${ir.annotation.intensity},${ir.annotation.replaced}," +
            s"${ir.annotation.retentionIndex},${ir.annotation.nonCorrectedRt},${ir.annotation.rtDistance}," +
            s"${ir.annotation.mass},${ir.annotation.massError},${ir.annotation.massErrorPPM}")
        pw.flush()
      })
    }))
    pw.close()
  }
}
