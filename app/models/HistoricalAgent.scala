package models

/**
 * Classes representing an ISDIAH collection-holding institution
 */

import defines.{EntityType, PublicationStatus, enum}
import base._

import play.api.libs.json._
import defines.EnumWriter.enumWrites

object HistoricalAgentF {

  final val DESC_REL = "describes"
  final val ADDRESS_REL = "hasAddress"

  final val UNNAMED_PLACEHOLDER = "UNNAMED Authority"

  val NAME = "name"
  val PUBLICATION_STATUS = "publicationStatus"
}

case class HistoricalAgentF(
  id: Option[String],
  identifier: String,
  name: String,
  publicationStatus: Option[PublicationStatus.Value] = None,
  @Annotations.Relation(HistoricalAgentF.DESC_REL) descriptions: List[HistoricalAgentDescriptionF] = Nil
) extends Persistable {
  val isA = EntityType.HistoricalAgent

  import json.HistoricalAgentFormat._
  def toJson: JsValue = Json.toJson(this)
}

case class HistoricalAgent(val e: Entity)
  extends NamedEntity
  with AccessibleEntity
  with AnnotatableEntity
  with DescribedEntity
  with Formable[HistoricalAgentF] {
  override def descriptions: List[HistoricalAgentDescription] = e.relations(DescribedEntity.DESCRIBES_REL).map(HistoricalAgentDescription(_))

  val publicationStatus = e.property(HistoricalAgentF.PUBLICATION_STATUS).flatMap(enum(PublicationStatus).reads(_).asOpt)

  import json.HistoricalAgentFormat._
  lazy val formable: HistoricalAgentF = Json.toJson(e).as[HistoricalAgentF]
  lazy val formableOpt: Option[HistoricalAgentF] = Json.toJson(e).asOpt[HistoricalAgentF]

  override def toString = {
    descriptions.headOption.flatMap(d => d.stringProperty(Isdiah.AUTHORIZED_FORM_OF_NAME)).getOrElse(identifier)
  }
}



