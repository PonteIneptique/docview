package backend.rest

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.libs.json.{Reads, Json}
import backend.{BackendReadable, ApiUser}
import utils.search.{Resolver, SearchHit}


trait SearchDAO extends RestDAO {

  import play.api.Play.current

  def requestUrl = "http://%s:%d/%s/entities".format(host, port, mount)

  def getAny[MT](id: String)(implicit apiUser: ApiUser,  rd: BackendReadable[MT]): Future[MT] = {
    WS.url(enc(requestUrl, id)).withHeaders(authHeaders.toSeq: _*).get().map { response =>
      checkErrorAndParse(response)(rd.restReads)
    }
  }

  def listByGid[MT](ids: Seq[Long])(implicit apiUser: ApiUser,  rd: BackendReadable[MT]): Future[Seq[MT]] = {
    // NB: Using POST here because the list of IDs can
    // potentially overflow the GET param length...
    if (ids.isEmpty) Future.successful(Seq.empty[MT])
    else WS.url(enc(requestUrl, "listByGraphId"))
        .withHeaders(authHeaders.toSeq: _*).post(Json.toJson(ids)).map { response =>
      checkErrorAndParse(response)(Reads.seq(rd.restReads))
    }
  }

  def list[MT](ids: Seq[String])(implicit apiUser: ApiUser,  rd: BackendReadable[MT]): Future[Seq[MT]] = {
    // NB: Using POST here because the list of IDs can
    // potentially overflow the GET param length...
    if (ids.isEmpty) Future.successful(Seq.empty[MT])
    else WS.url(requestUrl).withHeaders(authHeaders.toSeq: _*).post(Json.toJson(ids)).map { response =>
      checkErrorAndParse(response)(Reads.seq(rd.restReads))
    }
  }
}

object SearchDAO extends SearchDAO

/**
 * Resolve search hits to DB items by the GID field
 */
case class GidSearchResolver() extends RestDAO with SearchDAO with Resolver {
  def resolve[MT](docs: Seq[SearchHit])(implicit apiUser: ApiUser,  rd: BackendReadable[MT]): Future[Seq[MT]] =
    listByGid(docs.map(_.gid))
}

/**
 * Resolve search hits to DB items by the itemId field
 */
case class IdSearchResolver() extends RestDAO with SearchDAO with Resolver {
  def resolve[MT](docs: Seq[SearchHit])(implicit apiUser: ApiUser,  rd: BackendReadable[MT]): Future[Seq[MT]] =
    list(docs.map(_.itemId))
}
