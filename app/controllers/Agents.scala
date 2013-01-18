package controllers

import play.api._
import play.api.mvc._
import play.api.i18n.Messages
import defines._
import base._
import models.{DocumentaryUnit, Agent}
import models.forms.{AgentF,DocumentaryUnitF}
import java.lang.ProcessBuilder.Redirect


object Agents extends CreationContext[DocumentaryUnitF,Agent]
		with VisibilityController[AgentF,Agent]
		with CRUD[AgentF,Agent]
    with PermissionScopeController[Agent] {

  val targetContentTypes = Seq(ContentType.DocumentaryUnit)
  val childContentType = ContentType.DocumentaryUnit
  val childEntityType = EntityType.DocumentaryUnit


  val entityType = EntityType.Agent
  val contentType = ContentType.Agent

  val form = models.forms.AgentForm.form
  val childForm = models.forms.DocumentaryUnitForm.form
  val builder = Agent

  def get(id: String) = getAction(id) { item =>
    implicit maybeUser =>
      implicit request =>
      Ok(views.html.agent.show(Agent(item), maybeUser, request))
  }

  def list(page: Int = 1, limit: Int = DEFAULT_LIMIT) = listAction(page, limit) { page =>
    implicit maybeUser =>
      implicit request =>
        Ok(views.html.agent.list(page.copy(list = page.list.map(Agent(_))), maybeUser, request))
  }

  def create = withContentPermission(PermissionType.Create, contentType) { implicit user =>
    implicit request =>
      Ok(views.html.agent.edit(None, form, routes.Agents.createPost, user, request))
  }

  def createPost = createPostAction(models.forms.AgentForm.form) { formOrItem =>
    implicit user =>
      implicit request =>
    formOrItem match {
      case Left(form) => BadRequest(views.html.agent.edit(None, form, routes.Agents.createPost, user, request))
      case Right(item) => Redirect(routes.Agents.get(item.id))
        .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasCreated", item.id))
    }
  }

  def update(id: String) = updateAction(id) { item => implicit user =>
    implicit request =>
      Ok(views.html.agent.edit(Some(Agent(item)), form.fill(Agent(item).to), routes.Agents.updatePost(id), user, request))
  }

  def updatePost(id: String) = updatePostAction(id, form) { formOrItem =>
    implicit user =>
      implicit request =>
        formOrItem match {
          case Left(form) => getEntity(id, Some(user)) { item =>
            BadRequest(views.html.agent.edit(Some(Agent(item)), form, routes.Agents.updatePost(id), user, request))
          }
          case Right(item) => Redirect(routes.Agents.get(item.id))
            .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasUpdated", item.id))
        }
  }

  def createDoc(id: String) = childCreateAction(id) { item => implicit user =>
    implicit request =>
      Ok(views.html.documentaryUnit.create(
        Agent(item), childForm, routes.Agents.createDocPost(id), user, request))
  }

  def createDocPost(id: String) = childCreatePostAction(id, childForm) { formOrItem =>
    implicit user =>
      implicit request =>
        formOrItem match {
          case Left(form) => getEntity(id, Some(user)) { item =>
            BadRequest(views.html.documentaryUnit.create(Agent(item),
              form, routes.Agents.createDocPost(id), user, request))
          }
          case Right(item) => Redirect(routes.DocumentaryUnits.get(item.id))
            .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasCreate", item.id))
        }
  }

  def delete(id: String) = deleteAction(id) { item => implicit user =>
    implicit request =>
      Ok(views.html.delete(
        Agent(item), routes.Agents.deletePost(id),
        routes.Agents.get(id), user, request))
  }

  def deletePost(id: String) = deletePostAction(id) { ok => implicit user =>
    implicit request =>
      Redirect(routes.Agents.list())
        .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasDeleted", id))
  }

  def visibility(id: String) = visibilityAction(id) { item => users => groups => implicit user =>
    implicit request =>
      Ok(views.html.visibility(Agent(item), users, groups, routes.Agents.visibilityPost(id), user, request))
  }

  def visibilityPost(id: String) = visibilityPostAction(id) { ok => implicit user =>
    implicit request =>
      Redirect(routes.Agents.list())
        .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasUpdated", id))
  }

  def managePermissions(id: String, page: Int = 1, spage: Int = 1, limit: Int = DEFAULT_LIMIT) = manageScopedPermissionsAction(id, page, spage, limit) {
    item => perms => sperms => implicit user => implicit request =>
      Ok(views.html.permissions.manageScopedPermissions(Agent(item), perms, sperms,
        routes.Agents.addItemPermissions(id), routes.Agents.addScopedPermissions(id), user, request))
  }

  def addItemPermissions(id: String) = addItemPermissionsAction(id) {
    item => users => groups => implicit user => implicit request =>
      Ok(views.html.permissions.permissionItem(Agent(item), users, groups,
        routes.Agents.setItemPermissions _, user, request))
  }

  def addScopedPermissions(id: String) = addItemPermissionsAction(id) {
    item => users => groups => implicit user => implicit request =>
      Ok(views.html.permissions.permissionItem(Agent(item), users, groups,
        routes.Agents.setScopedPermissions _, user, request))
  }

  def setItemPermissions(id: String, userType: String, userId: String) = setItemPermissionsAction(id, userType, userId) {
    item => accessor => perms => implicit user => implicit request =>
      Ok(views.html.permissions.setPermissionItem(Agent(item), accessor, perms, contentType,
        routes.Agents.setItemPermissionsPost(id, userType, userId), user, request))
  }

  def setItemPermissionsPost(id: String, userType: String, userId: String) = setItemPermissionsPostAction(id, userType, userId) {
    bool => implicit user => implicit request =>
      Redirect(routes.Agents.managePermissions(id))
        .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasUpdated", id))
  }

  def setScopedPermissions(id: String, userType: String, userId: String) = setScopedPermissionsAction(id, userType, userId) {
    item => accessor => perms => implicit user => implicit request =>
      Ok(views.html.permissions.setPermissionScope(Agent(item), accessor, perms, targetContentTypes,
        routes.Agents.setScopedPermissionsPost(id, userType, userId), user, request))
  }

  def setScopedPermissionsPost(id: String, userType: String, userId: String) = setScopedPermissionsPostAction(id, userType, userId) {
    perms => implicit user => implicit request =>
      Redirect(routes.Agents.managePermissions(id))
        .flashing("success" -> play.api.i18n.Messages("confirmations.itemWasUpdated", id))
  }
}