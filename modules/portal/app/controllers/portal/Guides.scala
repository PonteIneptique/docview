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

  def layoutRetrieval(key: String) = {

      key match {
        case "people" => {
          val layout = "person"
          val params = Map("holderId" -> "ehri-cb")
          guideLayout(layout, params)
        }
        case "keywords" => {
          val layout = "keyword"
          val params = Map("holderId" -> "ehri-camps")
          guideLayout(layout, params)
        }

      }
  }

  def guideLayout(layout: String, params: Any) = {
      /* Will be replaced by MySQL stuff */
      /* Function mapping */
      layout match {
        case "person" => {
          params match {
            case p: Map[String, String] => {
              guideAuthority(p)
            }
          }
        }
        case "keyword" => {
          params match {
            case p: Map[String, String] => {
              guideKeyword(p)
            }
          }
        }
      }
  }

  def guideAuthority(params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[HistoricalAgent](params, defaultParams = Some(SearchParams(entities=List(EntityType.HistoricalAgent)))
      ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.person(page, params, facets, portalRoutes.browseHistoricalAgents()))
    }.apply(request)
  }

  def guideKeyword(params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(page, params, facets, portalRoutes.browseConcepts(),
        userDetails.watchedItems))
    }.apply(request)
  }

}