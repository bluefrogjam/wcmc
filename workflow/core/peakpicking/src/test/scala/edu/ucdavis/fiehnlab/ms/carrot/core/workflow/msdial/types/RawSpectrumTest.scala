package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types

import java.util.{ArrayList => JArrayList, List => JList}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{LocalDirectorySampleLoader, LocalDirectorySampleLoaderProperties}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by diego on 10/12/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RawSpectrumTestConfig]))
class RawSpectrumTest extends WordSpec {

  @Autowired
  var sampleLoader: LocalDirectorySampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  assert(sampleLoader !== null)

  "RawSpectrum" should {
    "convert a sample to RawSpectrum when called with sample" must {
      var sample = sampleLoader.loadSample("/msdial/sample.msdial")

      "have a valid Sample" in {
        assert(sample.isDefined)
        assert(sample.get.isInstanceOf[Sample])
      }

      "convert spectra to RawSpectrum list" in {
        var spectra = sample.get.spectra.map(spec => new RawSpectrum(spec)).asJava

        assert(spectra.isInstanceOf[JList[RawSpectrum]])
        assert(spectra.size() > 0)
      }
    }
  }
}

@Configuration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.ms.carrot.core.workflow"))
class RawSpectrumTestConfig {

  @Bean
  def localDirectorySampleLoader(properties: LocalDirectorySampleLoaderProperties): LocalDirectorySampleLoader = {
    properties.directories = ("src/test/resources" :: "./" :: List()).asJava

    new LocalDirectorySampleLoader(properties)
  }
}
