package com.github.dedkovva.front

import play.api.libs.json._
import com.github.dedkovva.shared._

/**
  * Created by dedkov-va on 07.04.18.
  */
object JsonReader {
  implicit val CountryFormat = Json.reads[Country]
  implicit val AirportFormat = Json.reads[Airport]
  implicit val RunwayFormat = Json.reads[Runway]
  implicit val ReportFormat = Json.reads[Report]
}
