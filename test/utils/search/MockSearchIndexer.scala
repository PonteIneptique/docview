package utils.search

import defines.EntityType
import utils.search._
import play.api.Logger
import scala.concurrent.Future

/**
 * User: michaelb
 *
 * TODO: Integrate better with fixtures.
 *
 */
case class MockSearchIndexer() extends Indexer {
  val eventBuffer = collection.mutable.ArrayBuffer.empty[String]
  def indexId(id: String) = {
    eventBuffer += id
    Logger.logger.info("Indexing: " + id)
    Future.successful()
  }
  def indexTypes(entityTypes: Seq[EntityType.Value]) = {
    eventBuffer += entityTypes.toString
    Logger.logger.info("Indexing: " + entityTypes)
    Future.successful()
  }
  def indexChildren(entityType: EntityType.Value, id: String) = {
    eventBuffer += id
    Logger.logger.info("Indexing children: " + entityType + " -> " + id)
    Future.successful()
  }
  def clearAll() = {
    eventBuffer += "clear-all"
    Logger.logger.info("Clearing entire index...")
    Future.successful()
  }
  def clearTypes(entityTypes: Seq[EntityType.Value]) = {
    eventBuffer += "clear-types:" + entityTypes.toString
    Logger.logger.info("Clearing entity types: " + entityTypes)
    Future.successful()
  }
  def clearId(id: String) = {
    eventBuffer += id
    Logger.logger.info("Clearing id: " + id)
    Future.successful()
  }
  def clearKeyValue(key: String, value: String) = {
    eventBuffer += "clear-key-value " + s"$key=$value"
    Logger.logger.info("Clearing key-value: " + s"$key=$value")
    Future.successful()
  }
}
