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

	def pages(path: String): List[GuidesPage] = DB.withConnection { implicit connection =>
		SQL(
			"""
			SELECT 
				rgp.*
			FROM 
				research_guide_page rgp,
				research_guide rg 
			WHERE 
				rg.id_research_guide = rgp.id_research_guide AND
				rg.path_research_guide = {path}
			"""
		).on('path -> path).apply().map { row =>
			GuidesPage(
				row[Int]("id_research_guide_page"),
				row[String]("layout_research_guide_page"),
				row[String]("name_research_guide_page"),
				row[String]("path_research_guide_page"),
				row[String]("menu_research_guide_page"),
				row[String]("cypher_research_guide_page"),
				row[Int]("id_research_guide")
			)
		}.toList
	}

	def g(path: String): GuidesData = DB.withConnection { implicit connection =>
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
		).on('guidePath -> path).apply().headOption.map { row =>
		  GuidesData(
		    row[Int]("id_research_guide"),
		    row[String]("name_research_guide"),
		    row[String]("path_research_guide"),
		    row[Option[String]]("picture_research_guide"),
		    row[Option[String]]("description_research_guide")
		  )
		}.head
	}

	def p(gPath: String, path: String): GuidesPage = DB.withConnection { implicit connection =>
		SQL(
			"""
			SELECT 
				rgp.*
			FROM 
				research_guide_page rgp,
				research_guide rg 
			WHERE 
				rg.id_research_guide = rgp.id_research_guide AND
				rg.path_research_guide = {gPath} AND
				rgp.path_research_guide_page = {path}
			"""
		).on('gPath -> gPath, 'path -> path).apply().map { row =>
			GuidesPage(
				row[Int]("id_research_guide_page"),
				row[String]("layout_research_guide_page"),
				row[String]("name_research_guide_page"),
				row[String]("path_research_guide_page"),
				row[String]("menu_research_guide_page"),
				row[String]("cypher_research_guide_page"),
				row[Int]("id_research_guide")
			)
		}.head
	}
  
	def listGuides() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.list(guides))
	}

	def edit(path: String) = userProfileAction { implicit userOpt => implicit request => 

		Ok(views.html.edit(formGuide.fill(g(path)), guides, guidesRoutes.edit(path)))
	}

	def create() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.create(formGuide, guides, guidesRoutes.create()))
	}

	/*
	*	Pages routes
	*/

	def listPages(path: String) = userProfileAction { implicit userOpt => implicit request =>
		Ok(views.html.p.list(pages(path), g(path), guides))
	}

	def editPages(gPath: String, path: String) = userProfileAction { implicit userOpt => implicit request =>
		Ok(views.html.p.edit(formPage.fill(p(gPath, path)), p(gPath, path), g(gPath), pages(gPath), guides, guidesRoutes.editPages(gPath, path)))
	}
	def createPages(gPath: String) = userProfileAction { implicit userOpt => implicit request =>
		Ok(views.html.p.create(formPage, g(gPath), pages(gPath), guides, guidesRoutes.createPages(gPath)))
	}
}