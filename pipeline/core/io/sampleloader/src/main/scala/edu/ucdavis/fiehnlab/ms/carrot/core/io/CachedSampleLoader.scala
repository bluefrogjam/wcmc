package edu.ucdavis.fiehnlab.ms.carrot.core.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{Cacheable, EnableCaching}
import org.springframework.context.annotation.Configuration

/**
  * provides simple temporary caching, utilizing EHCache
  */
class CachedSampleLoader @Autowired()(sampleLoader: SampleLoader) extends SampleLoader with LazyLogging {
  /**
    * loads a sample as an option, so that we can evaluate it we have it or not, without an exception
    *
    * @param name
    * @return
    */
  @Cacheable(value = Array("loadSampleCache"),key = "#name")
  override def loadSample(name: String): Option[Sample] = sampleLoader.loadSample(name)

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  @Cacheable(value = Array("existsSampleCache"),key = "#name")
  override def sampleExists(name: String): Boolean = sampleLoader.sampleExists(name)
}

@Configuration
@EnableCaching
class CachedSampleLoaderConfiguration