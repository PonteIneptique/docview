package utils.search

import scala.concurrent.Future
import backend.{BackendReadable, ApiUser}

/**
 * Component responsible for resolving items from the
 * database from items returned from the search engine.
 *
 * Different resolvers could use either global identifiers
 * or more optimised internal graph IDs for faster resolution.
 *
 * @author Mike Bryant (http://github.com/mikesname)
 */
trait Resolver {
  def resolve[MT](results: Seq[SearchHit])(implicit apiUser: ApiUser, rd: BackendReadable[MT]): Future[Seq[MT]]
}
