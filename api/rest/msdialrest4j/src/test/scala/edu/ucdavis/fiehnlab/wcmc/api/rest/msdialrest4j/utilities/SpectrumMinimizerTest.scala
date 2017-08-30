package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.utilities

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.MSDialRestProcessorConfig
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.{SpringBootTest, TestConfiguration}
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.io.Source

/**
	* Created by diego on 7/28/2017.
	*/
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[MSDialRestProcessorConfig], classOf[MinimizerTestConfig]))
class SpectrumMinimizerTest extends WordSpec with ShouldMatchers with LazyLogging {

	@Autowired
	val minimizer: Option[SpectrumMinimizer] = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"Minimize" should {
		val file = new File("h:/tmp/B5_P20Lipids_Pos_NIST01.msdial")

		val miniFile = minimizer.get.minimize(file)

		"return an existent file" in {
			miniFile.exists
		}

		"reduce spectrum size" in {
			miniFile.length() should be < file.length()
		}

		"have the same line number" in {
			val origFileLength = Source.fromFile(file).getLines().length
			val newFileLength = Source.fromFile(miniFile).getLines().length

			logger.debug(s"old lines: ${origFileLength}")
			logger.debug(s"new lines: ${newFileLength}")

			newFileLength should be (origFileLength)
		}
	}
}

@TestConfiguration
class MinimizerTestConfig {
	@Bean
	def minimizer: Option[SpectrumMinimizer] = Some(new SpectrumMinimizer())
}
