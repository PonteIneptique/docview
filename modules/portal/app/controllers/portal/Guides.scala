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
  def home() = userProfileAction { implicit userOpt => implicit request =>
   Ok(layoutRetrieval("places"))
  }

  def layoutRetrieval(key: String) = userProfileAction { implicit userOpt => implicit request =>
    val template = DB.withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM research_guide_page 
            WHERE path_research_guide_page = {id}
            LIMIT 1
          """
        ).on('id -> key).apply().head
      }

   Ok(guideLayout(template[String]("name_research_guide"), template[String]("path_research_guide_page"), template[String]("layout_research_guide_page"), Map("holderId" -> template[String]("cypher_research_guide_page"))))
 }

  def guideLayout(title:String, path: String, layout: String, params: Any) =  userProfileAction { implicit userOpt => implicit request =>
      /* Will be replaced by MySQL stuff */
      /* Function mapping */
      layout match {
        case "person" => {
          params match {
            case p: Map[String, String] => {
              Ok(guideAuthority(title, path, p))
            }
          }
        }
        case "map" => {
          params match {
            case p: Map[String, String] => {
              Ok(guideMap(title, path, p))
            }
          }
        }
        case "keyword" => {
          params match {
            case p: Map[String, String] => {
              Ok(guideKeyword(title, path, p))
            }
          }
        }
        case "organisation" => {
          params match {
            case p: Map[String, String] => {
              Ok(guideOrganization(title, path, p))
            }
          }
        }
        case "md" => {
          params match {
            case p: Map[String, String] => {
              for((key, content) <- p) {
                Ok(guideMarkdown(title, path, content))
              }
            }
          }
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
      Ok(p.guides.keywords(title, path, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

  def guideMap(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.places(title, path, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

  def guideOrganization(title: String, path: String, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(title, path, page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

  def guideMarkdown(title: String, path: String, content: String) = userProfileAction { implicit userOpt => implicit request =>
    Ok(p.guides.markdown(title, path, content))
  }

}