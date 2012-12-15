package rest

import play.api.libs.concurrent.execution.defaultContext
import scala.concurrent.Future
import play.api.libs.ws.WS
import models.Entity
import models.UserProfile
import scala.concurrent.Future
import play.api.libs.ws.WS
import models.{PermissionSet, Group}
import models.base.Accessor
import com.codahale.jerkson.Json
import defines._
import models.UserProfileRepr
import models.base.AccessibleEntity
import play.api.mvc.AnyContent
import play.api.libs.ws.Response
import play.api.mvc.RequestHeader
import play.api.mvc.Headers
import play.api.http.HeaderNames
import play.api.http.ContentTypes

case class ApiDAO(val userProfile: Option[UserProfileRepr]) extends RestDAO {

  def requestUrl = "http://%s:%d/%s".format(host, port, mount)

  def authHeaders: Map[String, String] = userProfile match {
    case Some(up) => (headers + (AUTH_HEADER_NAME -> up.id))
    case None => headers
  }
  

  def get(urlpart: String, headers: Headers): Future[Response] = {
    WS.url(enc("%s/%s".format(requestUrl, urlpart)))
    	.withHeaders(authHeaders.toSeq: _*).get
  }
}