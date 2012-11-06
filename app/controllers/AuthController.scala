package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play20.auth.{ Auth, LoginLogout }
import play.api.libs.concurrent.execution.defaultContext
import models.UserProfile
import models.AccessibleEntity
import play.api.libs.json.JsString
import defines.EntityType
import rest.PermissionDAO
import scala.concurrent.Future

/*
 * Wraps optionalUserAction to asyncronously fetch the User's profile.
 */
trait AuthController extends Controller with Auth with Authorizer {

  /**
   * Action composition that adds extra context to regular requests. Namely,
   * the profile of the user requesting the page, and her permissions.
   */
  def userProfileAction(f: Option[User] => Request[AnyContent] => Result): Action[AnyContent] = {
    optionalUserAction { implicit userOption =>
      implicit request =>
        userOption match {
          case Some(user) => {
            Async {
              // Since we know the user's profile_id we can get the real
              // details by using a fake profile to access their profile as them...
              val fakeProfile = UserProfile(None, user.profile_id, "", List())
              val profileRequest = rest.EntityDAO(EntityType.UserProfile, Some(fakeProfile)).get(user.profile_id)
              val permsRequest = rest.PermissionDAO(fakeProfile).get
              // These requests should execute in parallel...
              for {
                r1 <- profileRequest
                r2 <- permsRequest
              } yield {
                // Check nothing errored horribly...
                if (r1.isLeft) sys.error("Unable to fetch user profile: " + r1.left.get)
                if (r2.isLeft) sys.error("Unable to fetch user permissions: " + r2.left.get)

                // We're okay, so construct the complete profile.
                val u = user.withProfile(UserProfile(r1.right.get)).withPermissions(r2.right.get)
                f(Some(u))(request)
              }
            }
          }
          case None => f(userOption)(request)
        }
    }
  }
}