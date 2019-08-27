package com.github.dedkovva.shared

/**
  * Created by dedkov-va on 07.04.18.
  */
case class Report(
                   bottom10CountriesToAirportsNum: Seq[(Country, Int)],
                   top10CountriesToAirportsNum: Seq[(Country, Int)],
                   countriesToSurfaces: Seq[(Country, List[String])],
                   top10MostCommonRunwayIdents: Seq[(String, Long)]
                 )
case class Country(code: String, name: String)
case class Airport(id: String, name: String, iataCode: String, countryCode: String)
case class Runway(id: String, airportId: String, surface: String, ident: String)