package controllers.guides

import controllers.base.AuthController
import play.api.mvc.{Action, Controller}
import controllers.base.ControllerHelpers
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Execution.Implicits._

import play.api.db._
import play.api.Play.current

import com.google.inject._
import global.GlobalConfig
import backend.Backend
import play.api.Routes
import play.api.http.MimeTypes
import models.AccountDAO
import models.{GuidesData, GuidesPage}

import views.Helpers

import anorm._
import anorm.SqlParser._
case class GuidesAdmin @Inject()(implicit globalConfig: global.GlobalConfig, backend: Backend, userDAO: AccountDAO) extends Controller with AuthController with ControllerHelpers {

	private val formGuide = models.GuidesData.form
	private val formPage = models.GuidesPage.form
	private final val guidesRoutes = controllers.guides.routes.GuidesAdmin

	lazy val guides: List[GuidesData] = DB.withConnection { implicit connection =>
		SQL(
		"""
		SELECT * FROM research_guide
		"""
		).apply().map { row =>
			GuidesData(
			row[Int]("id_research_guide"),
			row[String]("name_research_guide"),
			row[String]("path_research_guide"),
			row[Option[String]]("picture_research_guide"),
			row[Option[String]]("description_research_guide")
			)
		}.toList
	}
  
	def listGuides() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.list(guides))
	}

	def edit(guidePath: String) = userProfileAction { implicit userOpt => implicit request => 
		val g: GuidesData = DB.withConnection { implicit connection =>
			SQL(
			  """
			    SELECT 
			      * 
			    FROM 
			      research_guide
			    WHERE 
			      path_research_guide = {guidePath} 
			    LIMIT 1
			  """
			).on('guidePath -> guidePath).apply().headOption.map { row =>
			  GuidesData(
			    row[Int]("id_research_guide"),
			    row[String]("name_research_guide"),
			    row[String]("path_research_guide"),
			    row[Option[String]]("picture_research_guide"),
			    row[Option[String]]("description_research_guide")
			  )
			}.head
		}
		Ok(views.html.edit(formGuide.fill(g), guides, guidesRoutes.edit(guidePath)))
	}

	def create() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.create(formGuide, guides, guidesRoutes.create()))
	}
}