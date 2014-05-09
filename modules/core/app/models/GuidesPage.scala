package models

import play.api.libs.json.{Reads, Format, Json}
import play.api.data.Form
import play.api.data.Forms._
import play.api.Mode.Mode
import org.joda.time.DateTime


/**
 * @author Thibault Clérice (http://github.com/ponteineptique)
 */
case class GuidesPage(
  objectId: Int,
  layout: String, 
  name: String, 
  path: String, 
  position: String, 
  content: String,
  parent: Int
)

object GuidesPage {

  val PREFIX = "guides.page"
  val OBJECTID = "objectId"
  val PATH = "path"
  val NAME = "name"
  val LAYOUT = "layout"
  val POSITION = "position"
  val CONTENT = "content"
  val PARENT = "parent"
  

  implicit val form = Form(
    mapping(
      "objectId" -> number,
      "layout" -> nonEmptyText,
      "name" -> nonEmptyText,
      "path" -> nonEmptyText,
      "position" -> nonEmptyText,
      "content" -> nonEmptyText,
      "parent" -> number
    )(GuidesPage.apply _)(GuidesPage.unapply _)
  )
}


