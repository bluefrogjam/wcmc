package edu.ucdavis.fiehnlab.wcmc.utilities.metadata

import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.impl.MzXMLMetadataExtraction
import org.springframework.beans.factory.{BeanFactory, NoSuchBeanDefinitionException}
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.{CustomConversions, DefaultDbRefResolver, DefaultMongoTypeMapper, MappingMongoConverter}
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Profile(Array("carrot.metadata.mongo"))
@Configuration
@EnableMongoRepositories
class MetadataConfiguration {
  @Bean
  def metadataExtraction: MetadataExtraction = new MzXMLMetadataExtraction


  @Bean def mappingMongoConverter(factory: MongoDbFactory, context: MongoMappingContext, beanFactory: BeanFactory): MappingMongoConverter = {
    val dbRefResolver = new DefaultDbRefResolver(factory)
    val mappingConverter = new MappingMongoConverter(dbRefResolver, context)
    try
      mappingConverter.setCustomConversions(beanFactory.getBean(classOf[CustomConversions]))
    catch {
      case ignore: NoSuchBeanDefinitionException =>

    }
    // Don't save _class to mongo
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null))
    mappingConverter
  }
}
