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
			row[Option[Int]]("id_research_guide"),
			row[String]("name_research_guide"),
			row[String]("path_research_guide"),
			row[Option[String]]("picture_research_guide"),
			row[Option[String]]("description_research_guide")
			)
		}.toList
	}

	def emptyPage(guideId: Option[Int]): GuidesPage = {
		GuidesPage(None, "", "", "", "", "", guideId.getOrElse(0))
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
				row[Option[Int]]("id_research_guide_page"),
				row[String]("layout_research_guide_page"),
				row[String]("name_research_guide_page"),
				row[String]("path_research_guide_page"),
				row[String]("menu_research_guide_page"),
				row[String]("cypher_research_guide_page"),
				row[Int]("id_research_guide")
			)
		}.toList
	}

	def g(path: String): Option[GuidesData] = DB.withConnection { implicit connection =>
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
		    row[Option[Int]]("id_research_guide"),
		    row[String]("name_research_guide"),
		    row[String]("path_research_guide"),
		    row[Option[String]]("picture_research_guide"),
		    row[Option[String]]("description_research_guide")
		  )
		}
	}

	def p(gPath: String, path: String): Option[GuidesPage] = DB.withConnection { implicit connection =>
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
		).on('gPath -> gPath, 'path -> path).apply().headOption.map { row =>
			GuidesPage(
				row[Option[Int]]("id_research_guide_page"),
				row[String]("layout_research_guide_page"),
				row[String]("name_research_guide_page"),
				row[String]("path_research_guide_page"),
				row[String]("menu_research_guide_page"),
				row[String]("cypher_research_guide_page"),
				row[Int]("id_research_guide")
			)
		}
	}

	def saveGuide(name: String, path: String, picture: Option[String], description: Option[String]): Option[Long] = DB.withConnection { implicit connection =>
		SQL(
			"""
			INSERT INTO
				research_guide
			(name_research_guide,path_research_guide, picture_research_guide, description_research_guide)
			VALUES
			({n}, {p}, {pi}, {de})
			"""
		).on('n -> name, 'p -> path, 'pi -> picture, 'de -> description).executeInsert()
	}



	def updateGuide(id:Option[Int], name: String, path: String, picture: Option[String], description: Option[String]): Int = DB.withConnection { implicit connection =>
		SQL(
			"""
			UPDATE
				research_guide
			SET 
				name_research_guide = {n},
				path_research_guide = {p},
				picture_research_guide = {pi},
				description_research_guide = {de}
			WHERE 
				id_research_guide = {i}
			LIMIT 1
			"""
		).on('n -> name, 'p -> path, 'pi -> picture, 'de -> description, 'i -> id).executeUpdate()
	}


	def savePage(layout: String, name: String, path: String, menu: String, cypher: String, parent: Int): Option[Long] = DB.withConnection { implicit connection =>
		SQL(
			"""

			INSERT INTO
				research_guide_page
			(
				layout_research_guide_page, 
				name_research_guide_page, 
				path_research_guide_page, 
				menu_research_guide_page, 
				cypher_research_guide_page, 
				id_research_guide
			)
			VALUES
			({l}, {n}, {p}, {m}, {c}, {parent})
			"""
		).on('l -> layout, 'n -> name, 'p -> path, 'm -> menu, 'c -> cypher, 'parent -> parent).executeInsert()
	}

	def updatePage(id: Option[Int], layout: String, name: String, path: String, menu: String, cypher: String, parent: Int): Int = DB.withConnection { implicit connection =>
		SQL(
			"""

			UPDATE
				research_guide_page
			SET
				layout_research_guide_page = {l}, 
				name_research_guide_page = {n}, 
				path_research_guide_page = {p}, 
				menu_research_guide_page = {m}, 
				cypher_research_guide_page = {c}, 
				id_research_guide = {parent}
			WHERE
				id_research_guide_page = {id}
			LIMIT 1
			"""
		).on('l -> layout, 'n -> name, 'p -> path, 'm -> menu, 'c -> cypher, 'parent -> parent, 'id -> id).executeUpdate()
	}
  
	def listGuides() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.list(guides))
	}

	def edit(path: String) = userProfileAction { implicit userOpt => implicit request => 
		g(path) match {
			case Some(gui) => Ok(views.html.edit(formGuide.fill(gui), guides, guidesRoutes.editPost(path)))
			case _ => Ok(views.html.list(guides))
		}
	}

	def editPost(path: String) = userProfileAction { implicit userOpt => implicit request => 
		g(path) match {
			case Some(gui) => {
				formGuide.bindFromRequest.fold(
					errorForm => {
						BadRequest(views.html.edit(errorForm, guides, guidesRoutes.editPost(path)))
					}, {
						case GuidesData(id, name, path, picture, description) =>
							updateGuide(gui.objectId, name, path, picture, description) match {
								case 1 => Ok(views.html.list(guides))
								case _ => Ok(views.html.edit(formGuide.fill(gui), guides, guidesRoutes.editPost(path)))
							}
					}
				)
			}
			case _ => Ok(views.html.list(guides))
		}
	}

	def create() = userProfileAction { implicit userOpt => implicit request => 
		Ok(views.html.create(formGuide, guides, guidesRoutes.createPost))
	}

	def createPost() = userProfileAction { implicit userOpt => implicit request => 
		formGuide.bindFromRequest.fold(
	      errorForm => {
	          BadRequest(views.html.create(formGuide, guides, guidesRoutes.createPost))
	      },
	      {
	        case GuidesData(_, name, path, picture, description) =>
	        	saveGuide(name, path, picture, description) match {
	        		case Some(i) => Ok(views.html.list(guides))
	        		case _ => Ok(views.html.create(formGuide, guides, guidesRoutes.createPost))
	        	}
				
	      }
	    )
	}

	/*
	*	Pages routes
	*/

	def listPages(path: String) = userProfileAction { implicit userOpt => implicit request =>
		g(path) match {
			case Some(gui) => Ok(views.html.p.list(pages(path), gui, guides))
			case _ => Ok(views.html.list(guides))
		}
	}

	def editPages(gPath: String, path: String) = userProfileAction { implicit userOpt => implicit request =>

		g(gPath) match {
			case Some(gui) => {
				p(gPath, path) match {
					case Some(pageLayout) => Ok(views.html.p.edit(formPage.fill(pageLayout), pageLayout, gui, pages(gPath), guides, guidesRoutes.editPagesPost(gPath, path)))
					case _ => BadRequest(views.html.p.list(pages(gPath), gui, guides))
				}
				
			}
			case _ => Ok(views.html.list(guides))
		}
		

	}

	def editPagesPost(gPath: String, path: String) = userProfileAction { implicit userOpt => implicit request =>
		g(gPath) match {
			case Some(gui) => {
				p(gPath, path) match {
					case Some(pageLayout) => 
						formPage.bindFromRequest.fold(
					      errorForm => {
					          BadRequest(views.html.p.edit(errorForm, pageLayout, gui, pages(gPath), guides, guidesRoutes.editPagesPost(gPath, path)))
					      },
					      {
					        case GuidesPage(id, layout, name, path, menu, cypher, parent) =>
					        	updatePage(id, layout, name, path, menu, cypher, parent) match {
					        		case 1 => Ok(views.html.p.list(pages(gPath), gui, guides))
					        		case _ => BadRequest(views.html.p.edit(formPage.fill(pageLayout), pageLayout, gui, pages(gPath), guides, guidesRoutes.editPagesPost(gPath, path)))
					        	}
								
					      }
					    )
					case _ => BadRequest(views.html.p.list(pages(gPath), gui, guides))
			    }
				
			
			}
			case _ => Ok(views.html.list(guides))
		}
		
	}

	def createPages(gPath: String) = userProfileAction { implicit userOpt => implicit request =>
		g(gPath) match {
			case Some(gui) => Ok(views.html.p.create(formPage.fill(emptyPage(gui.objectId)), gui, pages(gPath), guides, guidesRoutes.createPagesPost(gPath)))
			case _ => Ok(views.html.list(guides))
		}
		
	}
	def createPagesPost(gPath: String) = userProfileAction { implicit userOpt => implicit request =>
		g(gPath) match {
			case Some(gui) => {
				formPage.bindFromRequest.fold(
			      errorForm => {
			          BadRequest(views.html.p.create(errorForm, gui, pages(gPath), guides, guidesRoutes.createPagesPost(gPath)))
			      },
			      {
			        case GuidesPage(_, layout, name, path, menu, cypher, parent) =>
			        	savePage(layout, name, path, menu, cypher, parent) match {
			        		case Some(i) => Ok(views.html.p.list(pages(gPath), gui, guides))
			        		case _ => BadRequest(views.html.p.create(formPage.fill(emptyPage(gui.objectId)), gui, pages(gPath), guides, guidesRoutes.createPagesPost(gPath)))
			        	}
						
			      }
			    )
				
			
			}
			case _ => Ok(views.html.list(guides))
		}
		
	}
}