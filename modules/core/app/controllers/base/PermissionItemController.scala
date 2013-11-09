package controllers.base

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import models.base._
import defines._
import models.{PermissionGrant, UserProfile}
import models.json.RestReadable
import utils.PageParams

/**
 * Trait for setting permissions on an individual item.
 */
trait PermissionItemController[MT] extends EntityRead[MT] {

  def manageItemPermissionsAction(id: String)(
      f: MT => rest.Page[PermissionGrant] => Option[UserProfile] => Request[AnyContent] => SimpleResult)(
      implicit rd: RestReadable[MT]) = {
    withItemPermission.async[MT](id, PermissionType.Grant, contentType) { item => implicit userOpt => implicit request =>
      val params = PageParams.fromRequest(request)
      backend.listItemPermissionGrants(id, params).map { permGrants =>
        f(item)(permGrants)(userOpt)(request)
      }
    }
  }

  def addItemPermissionsAction(id: String)(
      f: MT => Seq[(String,String)] => Seq[(String,String)] => Option[UserProfile] => Request[AnyContent] => SimpleResult)(
      implicit rd: RestReadable[MT]) = {
    withItemPermission.async[MT](id, PermissionType.Grant, contentType) { item => implicit userOpt => implicit request =>
      getUsersAndGroups { users => groups =>
        f(item)(users)(groups)(userOpt)(request)
      }
    }
  }


  def setItemPermissionsAction(id: String, userType: String, userId: String)(
      f: MT => Accessor => acl.ItemPermissionSet[Accessor] => Option[UserProfile] => Request[AnyContent] => SimpleResult)(
      implicit rd: RestReadable[MT]) = {
    withItemPermission.async[MT](id, PermissionType.Grant, contentType) { item => implicit userOpt => implicit request =>
      for {
        accessor <- backend.get[Accessor](EntityType.withName(userType), userId)
        perms <- backend.getItemPermissions(accessor, contentType, id)
      } yield f(item)(accessor)(perms.copy(user=accessor))(userOpt)(request)
    }
  }

  def setItemPermissionsPostAction(id: String, userType: String, userId: String)(
      f: acl.ItemPermissionSet[Accessor] => Option[UserProfile] => Request[AnyContent] => SimpleResult)(
      implicit rd: RestReadable[MT]) = {
    withItemPermission.async[MT](id, PermissionType.Grant, contentType) { item => implicit userOpt => implicit request =>
      val data = request.body.asFormUrlEncoded.getOrElse(Map())
      val perms: List[String] = data.get(contentType.toString).map(_.toList).getOrElse(List())
      for {
        accessor <- backend.get[Accessor](EntityType.withName(userType), userId)
        perms <- backend.setItemPermissions(accessor, contentType, id, perms)
      } yield f(perms)(userOpt)(request)
    }
  }
}

