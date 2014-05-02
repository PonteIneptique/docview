package controllers.portal

import play.api.Play.current
import controllers.generic.Search
import models._
import models.base.AnyModel
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import views.html.p
import utils.search._
import play.api.libs.json.Json
import play.api.cache.Cached
import defines.EntityType
import play.api.libs.ws.WS
import play.api.templates.Html
import solr.SolrConstants
import backend.Backend
import controllers.base.{SessionPreferences, ControllerHelpers}
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.Logger
import utils._
import language.postfixOps
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import com.google.inject._
import play.api.mvc.Results._
import views.html.errors.pageNotFound

case class Guides @Inject()(implicit globalConfig: global.GlobalConfig, searchDispatcher: Dispatcher, searchResolver: Resolver, backend: Backend,
                            userDAO: AccountDAO)
  extends ControllerHelpers
  with Search
  with FacetConfig
  with PortalActions
  with SessionPreferences[SessionPrefs] {

  val defaultPreferences = new SessionPrefs

  private val portalRoutes = controllers.portal.routes.Portal

  /**
   * Full text search action that returns a complete page of item data.
   * @return
   */
  private implicit val anyModelReads = AnyModel.Converter.restReads
  private val defaultSearchTypes = List(EntityType.Repository, EntityType.DocumentaryUnit, EntityType.HistoricalAgent,
    EntityType.Country)
  private val defaultSearchParams = SearchParams(entities = defaultSearchTypes, sort = Some(SearchOrder.Score))


  val menu:List[(String, List[(String, String)])] = DB.withConnection { implicit connection =>
    SQL(
      """
        SELECT 
          name_research_guide_page, 
          path_research_guide_page, 
          menu_research_guide_page
        FROM research_guide_page 
        WHERE id_research_guide = {id}
      """
    ).on('id -> 1).apply().map { row =>
      row[String]("menu_research_guide_page") -> List(row[String]("path_research_guide_page") -> row[String]("name_research_guide_page"))
    }.toList
  }

  /* Guides */
  def home() = {
    guideLayout("places")
  }

  def layoutRetrieval(key: String) = { 
    guideLayout(key)
 }

  def guideLayout(key: String) = {
    
    // Make a class to hold our template data
    case class TemplateData(layout: String, name: String, path: String, cypher: String)

    val template: Option[TemplateData] = DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM research_guide_page 
          WHERE path_research_guide_page = {id}
          LIMIT 1
        """
      ).on('id -> key).apply().headOption.map { row =>
        TemplateData(
          row[String]("layout_research_guide_page"),
          row[String]("name_research_guide_page"),
          row[String]("path_research_guide_page"),
          row[String]("cypher_research_guide_page")
        )
      }
    }

    /* Function mapping */
    template match {
      case Some(t) if t.layout == "person" => guideAuthority(t.name, t.path, Map("holderId" -> t.cypher))
      case Some(t) if t.layout == "map" => guideMap(t.name, t.path, Map("holderId" -> t.cypher))
      case Some(t) if t.layout == "keyword" => guideKeyword(t.name, t.path, Map("holderId" -> t.cypher))
      case Some(t) if t.layout == "organisation" => guideOrganization(t.name, t.path, Map("holderId" -> t.cypher))
      case Some(t) if t.layout == "md" => guideMarkdown(t.name, t.path, t.cypher)
      case _ => Action { implicit request =>
        NotFound(views.html.errors.pageNotFound())
      }
    }
  }

  def guideAuthority(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[HistoricalAgent](params, defaultParams = Some(SearchParams(entities=List(EntityType.HistoricalAgent)))
      ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.person(title, path, page, params, menu))

    }.apply(request)
  }

  def guideKeyword(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, path, page, params, menu))
    }.apply(request)
  }

  def guideMap(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.places(title, path, page, params, menu))
    }.apply(request)
  }

  def guideOrganization(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, path, page, params, menu))
    }.apply(request)
  }

  def guideMarkdown(title: String, path: String, content: String) = userProfileAction { implicit userOpt => implicit request =>
    Ok(p.guides.markdown(title, path, content, menu))
  }

}