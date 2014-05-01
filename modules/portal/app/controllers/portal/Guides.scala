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
      row[String]("menu_research_guide_page") -> List(row[String]("path_research_guide_page"), row[String]("name_research_guide_page"))
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
      val template = DB.withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM research_guide_page 
            WHERE path_research_guide_page = {id}
            LIMIT 1
          """
        ).on('id -> key).apply().head
      }

      /* Function mapping */
      template[String]("layout_research_guide_page") match {
        case "person" => {
          guideAuthority(template[String]("name_research_guide_page"), template[String]("path_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page")))
        }
        case "map" => {
          guideMap(template[String]("name_research_guide_page"), template[String]("path_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page")))
        }
        case "keyword" => {
          guideKeyword(template[String]("name_research_guide_page"), template[String]("path_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page")))
        }
        case "organisation" => {
          guideOrganization(template[String]("name_research_guide_page"), template[String]("path_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page")))
        }
        case "md" => {
          guideMarkdown(template[String]("name_research_guide_page"), template[String]("path_research_guide_page"), template[String]("cypher_research_guide_page"))
        }
        case "404" | _ => {
          guideMarkdown("404", "error", "#Error 404 \n Page unknown")
        }
      }
  }

  def guideAuthority(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[HistoricalAgent](params, defaultParams = Some(SearchParams(entities=List(EntityType.HistoricalAgent)))
      ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.person(title, path, page, params, facets, portalRoutes.browseHistoricalAgents()))

    }.apply(request)
  }

  def guideKeyword(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, path, page, params, facets, portalRoutes.browseConcepts(), userDetails.watchedItems))
    }.apply(request)
  }

  def guideMap(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.places(title, path, page, params, facets, portalRoutes.browseConcepts(), userDetails.watchedItems))
    }.apply(request)
  }

  def guideOrganization(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, path, page, params, facets, portalRoutes.browseConcepts(), userDetails.watchedItems))
    }.apply(request)
  }

  def guideMarkdown(title: String, path: String, content: String) = userProfileAction { implicit userOpt => implicit request =>
    Ok(p.guides.markdown(title, path, content, menu))
  }

}