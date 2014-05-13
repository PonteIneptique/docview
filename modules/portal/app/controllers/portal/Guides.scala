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
import models.GuidesData
import models.GuidesPage

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


  def menu(id: Int):List[GuidesPage] = DB.withConnection { implicit connection =>
    SQL(
      """
        SELECT 
          *
        FROM research_guide_page 
        WHERE id_research_guide = {id}
      """
    ).on('id -> id).apply().map { row =>
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

  def listGuides() = userProfileAction { implicit userOpt => implicit request => 

    val guides: List[GuidesData] = DB.withConnection { implicit connection =>
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
    Ok(p.guides.guidesList(guides))
  }

  /* Guides */
  def home(guide: String) = {
    guideLayout(guide, "places")
  }

  def layoutRetrieval(guide:String, key: String) = { 
    guideLayout(guide, key)
  }

  def guide(guideId: Int): GuidesData = DB.withConnection { implicit connection =>
    SQL(
      """
        SELECT 
          * 
        FROM 
          research_guide
        WHERE 
          id_research_guide = {id} 
        LIMIT 1
      """
    ).on('id -> guideId).apply().headOption.map { row =>
      GuidesData(
        row[Option[Int]]("id_research_guide"),
        row[String]("name_research_guide"),
        row[String]("path_research_guide"),
        row[Option[String]]("picture_research_guide"),
        row[Option[String]]("description_research_guide")
      )
    }.head
  }

  def guideLayout(guide:String, key: String) = {

    val template: Option[GuidesPage] = DB.withConnection { implicit connection =>

      SQL(
        """
          SELECT 
            rgp.id_research_guide_page,
            rgp.layout_research_guide_page,
            rgp.name_research_guide_page,
            rgp.path_research_guide_page,
            rgp.menu_research_guide_page,
            rgp.cypher_research_guide_page,
            rg.id_research_guide
          FROM 
            research_guide_page rgp,
            research_guide rg
          WHERE 
            rgp.path_research_guide_page = {id} AND 
            rgp.id_research_guide = rg.id_research_guide AND
            rg.path_research_guide = {guide}
          LIMIT 1
        """
      ).on('id -> key, 'guide -> guide).apply().headOption.map { row =>
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

    /* Function mapping */
    template match {
      case Some(t) if t.layout == "person" => guideAuthority(t, Map("holderId" -> t.content))
      case Some(t) if t.layout == "map" => guideMap(t, Map("holderId" -> t.content))
      case Some(t) if t.layout == "keyword" => guideKeyword(t, Map("holderId" -> t.content))
      case Some(t) if t.layout == "organisation" => guideOrganization(t, Map("holderId" -> t.content))
      case Some(t) if t.layout == "md" => guideMarkdown(t, t.content)
      case _ => Action { implicit request =>
        NotFound(views.html.errors.pageNotFound())
      }
    }
  }

  def getParams(request: Request[Any], eT: EntityType.Value ): Option[SearchParams] = {
    request.getQueryString("parent") match {
      case Some(parent) => Some(SearchParams(query = Some("parentId:" + parent), entities = List(eT)))
      case _ => Some(SearchParams(query = Some("isTopLevel:true"), entities = List(eT)))
    }
  }

  def guideAuthority(template: GuidesPage, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[HistoricalAgent](params, defaultParams = Some(SearchParams(entities=List(EntityType.HistoricalAgent)))
      ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.person(template -> (guide(template.parent) -> menu(template.parent)), page, params))

    }.apply(request)
  }

  def guideKeyword(template: GuidesPage, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(template -> (guide(template.parent) -> menu(template.parent)), page, params))
    }.apply(request)
  }

  def guideMap(template: GuidesPage, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = Some(SearchParams(entities = List(EntityType.Concept))),
      entityFacets = conceptFacets) {
        page => params => facets => _ => _ =>
      Ok(p.guides.places(template -> (guide(template.parent) -> menu(template.parent)), page, params))
    }.apply(request)
  }

  def guideOrganization(template: GuidesPage, params: Map[String, String]) = userBrowseAction.async { implicit userDetails => implicit request =>
    searchAction[Concept](params, defaultParams = getParams(request, EntityType.Concept),
      entityFacets = conceptFacets
    ) {
        page => params => facets => _ => _ =>
      Ok(p.guides.keywords(template -> (guide(template.parent) -> menu(template.parent)), page, params))
    }.apply(request)
  }

  def guideMarkdown(template: GuidesPage, content: String) = userProfileAction { implicit userOpt => implicit request =>
    Ok(p.guides.markdown(template -> (guide(template.parent) -> menu(template.parent)), content))
  }

}