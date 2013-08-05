package models

import defines.EntityType
import play.api.libs.json._
import play.api.data.validation.ValidationError
import play.api.libs.json.JsSuccess
import play.api.libs.json.util._
import play.api.libs.functional.syntax._

/**
 * User: michaelb
 */
package object json {

  /**
   * Reader for the EntityType enum
   */
  implicit val entityTypeReads = defines.EnumUtils.enumReads(EntityType)
  implicit val entityTypeFormat = defines.EnumUtils.enumFormat(EntityType)

  /**
   * Reads a generic entity.
   */
  implicit val entityReads: Reads[Entity] = (
    (__ \ Entity.ID).read[String] and
      (__ \ Entity.TYPE).read[EntityType.Type](defines.EnumUtils.enumReads(EntityType)) and
      (__ \ Entity.DATA).lazyRead(Reads.map[JsValue]) and
      (__ \ Entity.RELATIONSHIPS).lazyRead(Reads.map[List[Entity]](Reads.list(entityReads)))
    )(Entity.apply _)

  /**
   * Writes a generic entity.
   */
  implicit val entityWrites: Writes[Entity] = (
    (__ \ Entity.ID).write[String] and
      (__ \ Entity.TYPE).write[EntityType.Type](defines.EnumUtils.enumWrites) and
      (__ \ Entity.DATA).lazyWrite(Writes.map[JsValue]) and
      (__ \ Entity.RELATIONSHIPS).lazyWrite(Writes.map[List[Entity]])
    )(unlift(Entity.unapply))

  /**
   * Format for a generic entity.
   */
  val entityFormat: Format[Entity] = Format(entityReads, entityWrites)


  /**
   * Reads combinator that checks if a value is equal to the expected value.
   */
  def equalsReads[T](t: T)(implicit r: Reads[T]): Reads[T] = Reads.filter(ValidationError("validate.error.incorrectType", t))(_ == t)
  def equalsFormat[T](t: T)(implicit r: Format[T]): Format[T] = Format(equalsReads(t), r)

  /**
   * Reader that reads a possibly-non-existent field and supplies either the
   * value found or an empty list.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def nullableListReads[T](path: JsPath)(implicit fmt: Reads[T]): Reads[List[T]] = {
    new Reads[List[T]] {
      def reads(json: JsValue): JsResult[List[T]] = {
        json.validate[List[T]].fold(
          invalid = { err =>
            JsSuccess[List[T]](List.empty[T], path)
          },
          valid = { v =>
            JsSuccess[List[T]](v, path)
          }
        )
      }
    }
  }

  /**
   * Writer that writes a list field unless the list is empty, in which case
   * it writes nothing.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def nullableListWrites[T](path: JsPath)(implicit fmt: Writes[T]): OWrites[List[T]] = {
    new OWrites[List[T]] {
      def writes(o: List[T]): JsObject
          = if (o.isEmpty) Json.obj()
            else path.write[List[T]].writes(o)
    }
  }

  /**
   * Writes a list value as null if the list is empty. Reads as an empty list
   * if the path is null.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def nullableListFormat[T](path: JsPath)(implicit fmt: Format[T]): OFormat[List[T]] =
      OFormat[List[T]](nullableListReads(path)(fmt), nullableListWrites(path)(fmt))

  /**
   * Lazy alternative to nullableListReads.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def lazyNullableListReads[T](path: JsPath)(fmt: => Reads[T]): Reads[List[T]]
  = Reads(js => nullableListReads(path)(fmt).reads(js))

  /**
   * Lazy alternative to nullableListWrites.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def lazyNullableListWrites[T](path: JsPath)(fmt: => Writes[T]): OWrites[List[T]]
  = OWrites((t: List[T]) => nullableListWrites[T](path)(fmt).writes(t).as[JsObject])

  /**
   * Lazy alternative to nullableListFormat.
   * @param path
   * @param fmt
   * @tparam T
   * @return
   */
  def lazyNullableListFormat[T](path: JsPath)(fmt: => Format[T]): OFormat[List[T]]
      = OFormat[List[T]](lazyNullableListReads(path)(fmt), lazyNullableListWrites(path)(fmt))
}