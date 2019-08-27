package com.github.dedkovva.front

import com.github.dedkovva.shared.{Airport, Country, Report, Runway}
import japgolly.scalajs.react.component.Scala.BackendScope

import scala.concurrent.ExecutionContext.Implicits.global
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
  * Created by dedkov-va on 07.04.18.
  */
object Page {
  case class Props()
  case class State(
                    selected: String = "empty",
                    query: String = "",
                    result: String = "",
                    isLoading: Boolean = false,
                    isSuccess: Boolean = true
                  )

  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = {
      def onClick(): Callback = {
        $.modState(_.copy(result = "", isLoading = true)) >>
          (if (state.selected == "report") report() else query())
      }

      def query(): Callback = {
        Callback.future {
          RestClient.queryCountriesStats(state.query.trim).map(r => {
            $.modState(_.copy(result = queryResultToStr(r), isLoading = false, isSuccess = true))
          }).recover {
            case _: Throwable =>
              $.modState(_.copy(
                result = "Error occurred while getting query result", isLoading = false, isSuccess = false))
          }
        }
      }

      def report(): Callback = {
        Callback.future {
          RestClient.getReport.map(report => {
            $.modState(_.copy(result = reportToStr(report), isLoading = false, isSuccess = true))
          }).recover {
            case _: Throwable =>
              $.modState(_.copy(
                result = "Error occurred while getting report result", isLoading = false, isSuccess = false))
          }
        }
      }

      def reportToStr(r: Report): String = {
        def countryToStr(c: Country) = s"${c.code} | ${c.name}"
        s"""|BOTTOM 10 COUNTRIES BY NUM OF AIRPORTS:
            |COUNTRY CODE | COUNTRY NAME | NUM OR AIRPORTS
            |${r.bottom10CountriesToAirportsNum.map(e => s"${countryToStr(e._1)} | ${e._2}").mkString("\n")}
            |----------------------------------------------
            |
            |TOP 10 COUNTRIES BY NUM OF AIRPORTS:
            |COUNTRY CODE | COUNTRY NAME | NUM OR AIRPORTS
            |${r.top10CountriesToAirportsNum.map(e => s"${countryToStr(e._1)} | ${e._2}").mkString("\n")}
            |----------------------------------------------
            |
            |SURFACES PER COUNTRIES:
            |COUNTRY CODE | COUNTRY NAME | LIST OF SURFACES
            |${r.countriesToSurfaces.map(e => s"${countryToStr(e._1)} | ${e._2.mkString(",")}").mkString("\n")}
            |----------------------------------------------
            |
            |TOP 10 MOST COMMON RUNWAY IDENTS:
            |IDENT | NUM OF IDENTS
            |${r.top10MostCommonRunwayIdents.map(e => s"${e._1} | ${e._2}").mkString("\n")}
            |""".stripMargin
      }

      def queryResultToStr(r: Seq[(Country, Seq[(Airport, Seq[Runway])])]): String = {
        if (r.isEmpty) {
          s"Didn't find any information for country [${state.query.trim}]"
        } else {
          val header =
            """COUNTRY CODE | COUNTRY NAME | AIRPORT IATA | AIRPORT NAME | AIRPORT ID | RUNWAY ID
              |
              |----------------------------------------------------------------------------------
            """.stripMargin
          val queryResult = r.flatMap(e0 => {
            val c = s"${e0._1.code} | ${e0._1.name}"
            if (e0._2.isEmpty) List(s"$c | <wo airports>")
            else {
              e0._2.flatMap(e1 => {
                val a = s"${e1._1.iataCode} | ${e1._1.name} | ${e1._1.id}"
                if (e1._2.isEmpty) List(s"$c | $a | <wo runways>")
                else e1._2.map(r => s"$c | $a | ${r.id}")
              })
            }
          }).mkString("\n")
          s"$header\n$queryResult"
        }
      }

      <.form(^.cls := "form-horizontal",
        <.div(^.cls := "dimScreen" + (if (state.isLoading) "" else " hidden"),
          <.div(^.cls := "spinner")),
        <.div(^.cls := "form-group",
          <.label(^.cls := "control-label col-sm-1", ^.`for` := "select-action", "Action:"),
          <.div(^.cls := "col-sm-7",
            <.select(
              ^.id := "select-action",
              ^.cls := s"form-control",
              ^.onChange ==> ((e: ReactEventFromInput) => {
                val value = e.target.value
                $.modState(_.copy(selected = value, query = "", result = ""))
              }),
              <.option(^.value := "empty", ^.cls := "text-muted", "Choose action..."),
              <.option(^.value := "query", "Query"),
              <.option(^.value := "report", "Report")
            )
          )),
        <.div(^.cls := "form-group",
          <.label(^.cls := "control-label col-sm-1", ^.`for` := "select-action", "Country:"),
          <.div(^.cls := "col-sm-7",
            <.input(
              ^.id := "country",
              ^.placeholder := "Type country code or name (at least two characters)...",
              ^.`type` := "text",
              ^.cls := "form-control",
              ^.value := state.query,
              ^.onChange ==> ((e: ReactEventFromInput) => {
                val value = e.target.value
                $.modState(_.copy(query = value))
              })))
        ).when(state.selected == "query"),
        <.div(^.cls := "form-group",
          <.div(^.cls := "col-sm-3",
          <.input(^.`type` := "button", ^.cls := "btn btn-primary", ^.value := "Show result",
            ^.disabled := state.selected == "empty" || state.selected == "query" && state.query.trim.length < 2,
            ^.onClick --> onClick()))
        ),
        <.div(^.cls := "form-group",
          <.label(^.cls := "control-label col-sm-1", ^.`for` := "result", "Result:"),
          <.div(^.cls := "col-sm-7",
            <.textarea(
              ^.cls := "form-control" + (if (state.isSuccess) "" else " text-danger"),
              ^.rows := 15,
              ^.readOnly := true,
              ^.value := state.result)
          )
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("Page")
    .initialStateFromProps(props => State())
    .renderBackend[Backend]
    .build

  def apply() =
    component(Props())
}
