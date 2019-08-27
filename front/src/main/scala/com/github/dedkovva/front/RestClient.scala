package com.github.dedkovva.front

import com.github.dedkovva.shared._
import org.scalajs.dom
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by dedkov-va on 07.04.18.
  */
object RestClient {
  import com.github.dedkovva.front.JsonReader._

  private val urlPrefix = s"${Boot.restUrl}/${RestUrl.apiPrefix}"

  def getReport: Future[Report] = {
    dom.ext.Ajax.get(
      url = s"$urlPrefix/${RestUrl.report}"
    ).map(r => Json.parse(r.responseText).as[Report])
  }

  def queryCountriesStats(country: String): Future[Seq[(Country, Seq[(Airport, Seq[Runway])])]] = {
    dom.ext.Ajax.get(
      url = s"$urlPrefix/${RestUrl.query}/$country"
    ).map(r => {
      Json.parse(r.responseText).as[Seq[(Country, Seq[(Airport, Seq[Runway])])]]
    })
  }
}
