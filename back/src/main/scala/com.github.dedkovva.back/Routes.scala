package com.github.dedkovva.back

import java.util.Date

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.github.dedkovva.shared.RestUrl
import org.slf4j.LoggerFactory

/**
  * Created by dedkov-va on 07.04.18.
  */
object Routes extends Directives with JsonWriter {
  private val logger = LoggerFactory.getLogger(Routes.getClass)

  private val uiRoutes =
    path("favicon.ico") {
      get {
        getFromResource("favicon.ico")
      }
    } ~
      pathEndOrSingleSlash {
        redirect("/ui/index.html", StatusCodes.PermanentRedirect)
      } ~
      pathPrefix("ui") {
        getFromResourceDirectory("")
      } ~
      pathPrefix("js") {
        get {
          getFromResourceDirectory("js")
        }
      } ~
      pathPrefix("css") {
        get {
          getFromResourceDirectory("css")
        }
      }

  private val appRoutes = pathPrefix(RestUrl.apiPrefix) {
    pathPrefix(RestUrl.report) {
      get {
        val r = Service.report
        complete(r)
      }
    } ~ pathPrefix(RestUrl.query / Segment) { (country) =>
      get {
        val start = new Date()
        val r = Service.query(country)
        logger.info(s"query time: ${new Date().getTime - start.getTime}")
        complete(r)
      }
    }
  }

  val paths = uiRoutes ~ appRoutes
}
