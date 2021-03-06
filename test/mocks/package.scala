import models.MockAccount

package object mocks {

  val privilegedUser = MockAccount("mike", "example1@example.com", verified = true, staff = true)
  val unprivilegedUser = MockAccount("reto", "example2@example.com", verified = true, staff = true)
  val publicUser = MockAccount("joeblogs", "example@aol.com", verified = true, staff = false)
  val unverifiedUser = MockAccount("bobjohn", "example@yahoo.com", verified = false, staff = false)

  // Users...
  val users = Map(
    privilegedUser.id -> privilegedUser,
    unprivilegedUser.id -> unprivilegedUser,
    publicUser.id -> publicUser,
    unverifiedUser.id -> unverifiedUser
  )

  // Mutable map that serves as a mock db...
  var userFixtures = users
}