package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage

import com.amazonaws.services.dynamodbv2.datamodeling._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target, TargetAnnotation}
import org.socialsignin.spring.data.dynamodb.repository.{EnableScan, EnableScanCount}
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.repository.PagingAndSortingRepository

import scala.annotation.meta.field
import scala.beans.BeanProperty

@Profile(Array("carrot.store.result.dynamo"))
@DynamoDBTable(tableName = "carrot_result")
case class DynamoSample(

                         @BeanProperty
                         @(DynamoDBHashKey@field)
                         @(DynamoDBAutoGeneratedKey@field)
                         id: String,

                         @BeanProperty
                         @(DynamoDBAttribute@field)
                         fileName: String,

                         @BeanProperty
                         @(DynamoDBAttribute@field)
                         scan: Integer

                       )


@Profile(Array("carrot.store.result.dynamo"))
@EnableScan
trait DynamoSampleRepository extends PagingAndSortingRepository[DynamoSample, String] {

  def findByFileName(fileName: String, pageable: Pageable): Page[DynamoSample]

  @EnableScan
  @EnableScanCount
  def findAll(pageable: Pageable): Page[DynamoSample]
}
