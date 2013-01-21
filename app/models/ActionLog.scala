package models

import models.base.AccessibleEntity
import models.base.Accessor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ActionLog(val e: Entity) extends AccessibleEntity {

  val ACTIONER_REL = "hasActioner"

  val timeStamp: DateTime = e.property("timestamp").flatMap(_.asOpt[String]).map(new DateTime(_))
    .getOrElse(sys.error("No timestamp found on action [%s]".format(e.id)))
  val logMessage: String = e.property("logMessage").flatMap(_.asOpt[String]).getOrElse("No log message given.")
  val actioner: Option[Accessor] = e.relations(ACTIONER_REL).headOption.map(Accessor(_))

  def time = {
    DateTimeFormat.forPattern("yyyy-MM-dd HH:MM:ss").print(timeStamp)
  }
}

