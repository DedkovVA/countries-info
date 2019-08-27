package com.github.dedkovva.front

import japgolly.scalajs.react.extra.router.BaseUrl
import org.scalajs.dom

/**
  * Created by dedkov-va on 07.04.18.
  */
object Boot {
  val restUrl = BaseUrl.fromWindowOrigin.value

  def main(args: Array[String]): Unit = {
    val target = dom.document.getElementById("rootContainer")
    Page().renderIntoDOM(target)
  }
}
