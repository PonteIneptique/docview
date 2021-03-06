package backend

import scala.concurrent.{ExecutionContext, Future}
import models.UserProfile
import play.api.mvc.Headers
import play.api.libs.ws.WSResponse

/**
 * @author Mike Bryant (http://github.com/mikesname)
 */
trait Backend
  extends Generic
  with Permissions
  with Descriptions
  with Links
  with Annotations
  with Visibility
  with Events
  with Social
  with IdResolver {

  // Direct API queries
  def query(urlpart: String, headers: Headers, params: Map[String,Seq[String]] = Map.empty)(implicit apiUser: ApiUser, executionContext: ExecutionContext): Future[WSResponse]

  // Helpers
  def createNewUserProfile(data: Map[String,String] = Map.empty, groups: Seq[String] = Seq.empty)(implicit apiUser: ApiUser, executionContext: ExecutionContext): Future[UserProfile]
}
