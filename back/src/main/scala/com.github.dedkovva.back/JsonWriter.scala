package com.github.dedkovva.back

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.dedkovva.shared._
import spray.json.DefaultJsonProtocol
import spray.json._

/**
  * Created by dedkov-va on 12.04.18.
  */
trait JsonWriter extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val CountryFormat = jsonFormat2(Country)
  implicit val AirportFormat = jsonFormat4(Airport)
  implicit val RunwayFormat = jsonFormat4(Runway)
  implicit val ReportFormat = jsonFormat4(Report)
}
