package backend

import defines.EntityType
import scala.concurrent.{ExecutionContext, Future}

/**
 * Helper that generates IDs for content types.
 *
 * @author Mike Bryant (http://github.com/mikesname)
 */
trait IdGenerator {
  /**
   * Get the next ID for the given entity type by incrementing
   * the highest existing numerical ID by 1.
   */
  def getNextNumericIdentifier(entityType: EntityType.Value)(implicit executionContent: ExecutionContext): Future[String]

  /**
   * Get the next ID for the given entity type with the given
   * parent as a permission scope, by incrementing the highest
   * existing numerical ID by 1.
   */
  def getNextChildNumericIdentifier(parentId: String, entityType: EntityType.Value)(implicit executionContent: ExecutionContext): Future[String]
}
