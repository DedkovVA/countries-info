package com.github.dedkovva.back

import com.typesafe.config.ConfigFactory

/**
  * Created by dedkov-va on 07.04.18.
  */
object AppConfig {
  val config = ConfigFactory.load()
  object Http {
    private val http = config.getConfig("http")
    val host = http.getString("host")
    val port = http.getInt("port")
  }
}
