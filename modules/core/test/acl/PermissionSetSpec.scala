package acl

import play.api.test.PlaySpecification
import defines.{ContentTypes, PermissionType}
import play.api.libs.json.Json
import models.{UserProfileF, UserProfile}

/**
 * @author Mike Bryant (http://github.com/mikesname)
 */
class PermissionSetSpec extends PlaySpecification {

  val user = UserProfile(UserProfileF(id = Some("bob"), identifier = "bob", name = "Bob"))

  "GlobalPermissionSet" should {
    "parse correctly" in {
      val data: List[Map[String, Map[String, List[String]]]] = List(
        Map(
          "bob" -> Map(
            ContentTypes.DocumentaryUnit.toString -> List(
              PermissionType.Create.toString,
              PermissionType.Update.toString,
              "invalidPerm"
            ),
            "invalidEntity" -> List()
          )
        )
      )

      val permSet = Json.toJson(data).as[GlobalPermissionSet]
      permSet.has(ContentTypes.DocumentaryUnit, PermissionType.Create) must beTrue
      permSet.has(ContentTypes.DocumentaryUnit, PermissionType.Update) must beTrue
      permSet.has(ContentTypes.DocumentaryUnit, PermissionType.Annotate) must beFalse
    }
  }

  "ItemPermissionSet" should {
    "parse correctly" in {
      val data: List[Map[String, List[String]]] = List(
        Map(
          "bob" -> List(
            PermissionType.Create.toString,
            PermissionType.Update.toString,
            "invalidPerm"
          )
        )
      )

      val permSet = Json.toJson(data).as(ItemPermissionSet.restReads(ContentTypes.DocumentaryUnit))
      permSet.has(PermissionType.Create) must beTrue
      permSet.has(PermissionType.Update) must beTrue
      permSet.has(PermissionType.Annotate) must beFalse
    }
  }
}
