package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.binbase"))
@TestPropertySource(properties = Array(
  "carrot.sample:180501dngsa32_1.txt",
  "carrot.method:Gerstel | LECO-GC-TOF | rtx5recal | positive"
))
class RunnerTestForGCMS extends WordSpec {

  @Autowired
  val runner: Runner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  " a runner" should {
    "load the required sample and" must {
      "process it" in {
        runner.run()
      }
    }
  }
}


@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.lcms"))
@TestPropertySource(properties = Array(
  "carrot.sample:12345.mzML",
  "carrot.method:ABC"
))
class RunnerTestForLCMS extends WordSpec {

  @Autowired
  val runner: Runner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  " a runner" should {
    "load the required sample and" must {
      "process it" in {
        runner.run()
      }
    }
  }
}
