package models.base

import models.Entity
import defines.EntityType
import models.DocumentaryUnitDescription
import models.RepositoryDescription

object Description {
  def apply(e: Entity): Description = e.isA match {
    case EntityType.DocumentaryUnitDescription => DocumentaryUnitDescription(e)
    case EntityType.AgentDescription => RepositoryDescription(e)
    case _ => sys.error("Unknown description type: " + e.isA.toString())
  }
}


trait Description extends AccessibleEntity {
	val e: Entity
	
	val languageCode: String = e.property("languageCode").flatMap(_.asOpt[String]).getOrElse(sys.error("No language code found"))
}