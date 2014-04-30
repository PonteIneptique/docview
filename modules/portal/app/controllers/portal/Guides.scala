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

  /* Guides */
  def home() = {
    layoutRetrieval("places")
  }
  def layoutRetrieval(key: String = "") = {
    val template = DB.withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM research_guide_page 
            WHERE name_research_guide_page = {id}
            LIMIT 1
          """
        ).on('id -> key).apply().head
      }

   guideLayout(key, template[String]("layout_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page")))
 }

  def guideLayout(key:String, layout: String, params: Any) = {
      /* Will be replaced by MySQL stuff */
      /* Function mapping */
      layout match {
        case "person" => {
          params match {
            case p: Map[String, String] => {
              guideAuthority(key, p)
            }
          }
        }
        case "map" => {
          params match {
            case p: Map[String, String] => {
              guideMap(key, p)
            }
          }
        }
        case "keyword" => {
          params match {
            case p: Map[String, String] => {
              guideKeyword(key, p)
            }
          }
        }
        case "organisation" => {
          params match {
            case p: Map[String, String] => {
              guideOrganization(key, p)
            }
          }
        }
      }
  }

  def guideAuthority(title: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[HistoricalAgent](params, defaultParams = Some(SearchParams(entities=List(EntityType.HistoricalAgent)))
      ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.person(title, page, params, facets, portalRoutes.browseHistoricalAgents()))
    }.apply(request)
  }

  def guideKeyword(title: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

  def guideMap(title: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.places(title, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

  def guideOrganization(title: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

}