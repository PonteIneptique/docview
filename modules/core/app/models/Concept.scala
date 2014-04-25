package models

import base._

import defines.EntityType
import models.json._
import play.api.libs.json._
import eu.ehri.project.definitions.Ontology
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsObject


object ConceptF {

  val ACCESS_POINTS = "accessPoints"
  val UNKNOWN_DATA = "unknownData"

  val LANGUAGE = "languageCode"
  val PREFLABEL = "name"
  val ALTLABEL = "altLabel"
  val DEFINITION = "definition"
  val SCOPENOTE = "scopeNote"
  
  val LONGITUDE = "longitude"
  val LATITUDE = "latitude"

  val LONGITUDE = "longitude"
  val LATITUDE = "latitude"

  // NB: Type is currently unused...
  object ConceptType extends Enumeration {
    type Type = Value
  }

  import eu.ehri.project.definitions.Ontology._
  import play.api.libs.functional.syntax._
  import models.Entity._

  implicit val conceptWrites: Writes[ConceptF] = new Writes[ConceptF] {
    def writes(d: ConceptF): JsValue = {
      Json.obj(
        ID -> d.id,
        TYPE -> d.isA,
        DATA -> Json.obj(
          IDENTIFIER -> d.identifier
        ),
        RELATIONSHIPS -> Json.obj(
          DESCRIPTION_FOR_ENTITY -> Json.toJson(d.descriptions.map(Json.toJson(_)).toSeq)
        )
      )
    }
  }

  implicit val conceptReads: Reads[ConceptF] = (
    (__ \ TYPE).read[EntityType.Value](equalsReads(EntityType.Concept)) and
      (__ \ ID).readNullable[String] and
      (__ \ DATA \ IDENTIFIER).read[String] and
      (__ \ RELATIONSHIPS \ DESCRIPTION_FOR_ENTITY).lazyReadNullable[List[ConceptDescriptionF]](
        Reads.list[ConceptDescriptionF]).map(_.getOrElse(List.empty[ConceptDescriptionF]))
    )(ConceptF.apply _)

  implicit val conceptFormat: Format[ConceptF] = Format(conceptReads,conceptWrites)


  implicit object Converter extends RestConvertable[ConceptF] with ClientConvertable[ConceptF] {
    val restFormat = conceptFormat

    private implicit val conceptDscFmt = ConceptDescriptionF.Converter.clientFormat
    val clientFormat = Json.format[ConceptF]
  }
}

case class ConceptF(
  isA: EntityType.Value = EntityType.Concept,
  id: Option[String],
  identifier: String,
  @Annotations.Relation(Ontology.DESCRIPTION_FOR_ENTITY) descriptions: List[ConceptDescriptionF] = Nil
) extends Model with Persistable with Described[ConceptDescriptionF]


object Concept {
  import eu.ehri.project.definitions.Ontology._
  import play.api.libs.functional.syntax._
  import models.Entity._

  private implicit val systemEventReads = SystemEvent.Converter.restReads
  private implicit val vocabularyReads = Vocabulary.Converter.restReads

  implicit val metaReads: Reads[Concept] = (
    __.read[ConceptF] and
      (__ \ RELATIONSHIPS \ ITEM_IN_AUTHORITATIVE_SET).lazyReadNullable[List[Vocabulary]](
        Reads.list[Vocabulary]).map(_.flatMap(_.headOption)) and
      (__ \ RELATIONSHIPS \ CONCEPT_HAS_BROADER).lazyReadNullable[List[Concept]](
        Reads.list[Concept]).map(_.flatMap(_.headOption)) and
      (__ \ RELATIONSHIPS \ CONCEPT_HAS_BROADER).lazyReadNullable[List[Concept]](
        Reads.list[Concept]).map(_.getOrElse(List.empty[Concept])) and
      (__ \ RELATIONSHIPS \ IS_ACCESSIBLE_TO).lazyReadNullable[List[Accessor]](
        Reads.list(Accessor.Converter.restReads)).map(_.getOrElse(List.empty[Accessor])) and
      (__ \ RELATIONSHIPS \ ENTITY_HAS_LIFECYCLE_EVENT).lazyReadNullable[List[SystemEvent]](
        Reads.list[SystemEvent]).map(_.flatMap(_.headOption)) and
      (__ \ META).readNullable[JsObject].map(_.getOrElse(JsObject(Seq())))
    )(Concept.apply _)

  implicit object Converter extends ClientConvertable[Concept] with RestReadable[Concept] {
    val restReads = metaReads

    val clientFormat: Format[Concept] = (
      __.format[ConceptF](ConceptF.Converter.clientFormat) and
        (__ \ "vocabulary").formatNullable[Vocabulary](Vocabulary.Converter.clientFormat) and
        (__ \ "parent").lazyFormatNullable[Concept](clientFormat) and
        lazyNullableListFormat(__ \ "broaderTerms")(clientFormat) and
        nullableListFormat(__ \ "accessibleTo")(Accessor.Converter.clientFormat) and
        (__ \ "event").formatNullable[SystemEvent](SystemEvent.Converter.clientFormat) and
        (__ \ "meta").format[JsObject]
      )(Concept.apply _, unlift(Concept.unapply _))
  }

  implicit object Resource extends RestResource[Concept] {
    val entityType = EntityType.Concept
  }

  val form = Form(
    mapping(
      Entity.ISA -> ignored(EntityType.Concept),
      Entity.ID -> optional(nonEmptyText),
      Entity.IDENTIFIER -> nonEmptyText,
      "descriptions" -> list(ConceptDescription.form.mapping)
    )(ConceptF.apply)(ConceptF.unapply)
  )
}


case class Concept(
  model: ConceptF,
  vocabulary: Option[Vocabulary],
  parent: Option[Concept] = None,
  broaderTerms: List[Concept] = Nil,
  accessors: List[Accessor] = Nil,
  latestEvent: Option[SystemEvent],
  meta: JsObject = JsObject(Seq())
) extends AnyModel
  with MetaModel[ConceptF]
  with DescribedMeta[ConceptDescriptionF, ConceptF]
  with Hierarchical[Concept]
  with Accessible
  with Holder[Concept]