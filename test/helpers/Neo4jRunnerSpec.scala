package helpers

import org.specs2.mutable._
import org.specs2.specification.{Fragments, Step}
import eu.ehri.extension.AbstractAccessibleEntityResource
import play.api.test.{PlaySpecification, WithApplication}
import eu.ehri.extension.test.helpers.ServerRunner

/**
 * Specs2 magic to provide equivalent of JUnit's beforeClass/afterClass.
 */
trait BeforeAllAfterAll extends Specification {
  // see http://bit.ly/11I9kFM (specs2 User Guide)
  override def map(fragments: =>Fragments) =
    Step(beforeAll()) ^ fragments ^ Step(afterAll())

  protected def beforeAll()
  protected def afterAll()
}

/**
 * Abstract specification which initialises an instance of the
 * Neo4j server with the EHRI endpoint and cleans/sets-up the
 * test data before and after every test.
 */
abstract class Neo4jRunnerSpec(cls: Class[_]) extends PlaySpecification with UserFixtures with BeforeAllAfterAll with TestConfiguration {
  sequential

  val testPort = 7575
  def config = Map("neo4j.server.port" -> testPort) ++ getConfig

  val runner = ServerRunner.getInstance(testPort,
    classOf[AbstractAccessibleEntityResource[_]].getPackage.getName, "ehri")

  /**
   * Test running Fake Application. We have general all-test configuration,
   * handled in `config`, and per-test configuration (`specificConfig`) that
   * will be merged.
   * @param specificConfig A map of config values for this test
   */
  case class FakeApp(specificConfig: Map[String,Any] = Map.empty) extends WithApplication(
    fakeApplication(additionalConfiguration = config ++ specificConfig, global = getGlobal))

  /**
   * Tear down and setup fixtures before every test
   */
  override def before = {
    super.before
    runner.tearDownData()
    runner.setUpData()
  }

  /**
   * Start the server before every class test
   */
  def beforeAll() = runner.start()

  /**
   * Stop the server after every class test
   */
  def afterAll() = runner.stop()
}
