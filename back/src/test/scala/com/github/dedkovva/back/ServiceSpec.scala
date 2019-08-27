package com.github.dedkovva.back

import com.github.dedkovva.shared.{Airport, Country, Runway}
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created by dedkov-va on 11.04.18.
  */
class ServiceSpec extends FreeSpec with Matchers {
  "query spec" in {
    val r = Service.query("ar")
    r.size shouldBe 3

    r(0)._1 shouldBe Country("\"AR\"", "\"Armenia\"")
    r(0)._2.size shouldBe 1
    r(0)._2(0)._1 shouldBe Airport("08","\"Airport 08\"","\"A08\"","\"AR\"")
    r(0)._2(0)._2.size shouldBe 1
    r(0)._2(0)._2(0) shouldBe Runway("006","08","\"S01\"","\"AA\"")

    r(1)._1 shouldBe Country("\"NI\"", "\"Nicaragua\"")
    r(1)._2.size shouldBe 1
    r(1)._2(0)._1 shouldBe Airport("09","\"Airport 09\"","\"A09\"","\"NI\"")
    r(1)._2(0)._2.size shouldBe 0

    r(2)._1 shouldBe Country("AG", "Argentina")
    r(2)._2.size shouldBe 2
    r(2)._2(0)._1 shouldBe Airport("111","\"Airport 03\"","","AG")
    r(2)._2(0)._2.size shouldBe 0

    r(2)._2(1)._1 shouldBe Airport("AA","\"Airport 02\"","\"A02\"","AG")
    r(2)._2(1)._2.size shouldBe 2
    r(2)._2(1)._2(0) shouldBe Runway("04","AA","\"S02\"","\"AA\"")
    r(2)._2(1)._2(1) shouldBe Runway("044","AA","\"S01\"","\"BB\"")
  }

  "report spec" in {
    val report = Service.report
    report.bottom10CountriesToAirportsNum.size shouldBe 4
    report.bottom10CountriesToAirportsNum(0) shouldBe (Country("\"PN\"","\"Papua New Gunia\""), 0)
    report.bottom10CountriesToAirportsNum(1) shouldBe (Country("\"AR\"", "\"Armenia\""), 1)
    report.bottom10CountriesToAirportsNum(2) shouldBe (Country("\"NI\"", "\"Nicaragua\""), 1)
    report.bottom10CountriesToAirportsNum(3) shouldBe (Country("AG", "Argentina"), 2)

    report.top10CountriesToAirportsNum.size shouldBe 4
    report.top10CountriesToAirportsNum(0) shouldBe (Country("AG", "Argentina"), 2)
    report.top10CountriesToAirportsNum(1) shouldBe (Country("\"NI\"", "\"Nicaragua\""), 1)
    report.top10CountriesToAirportsNum(2) shouldBe (Country("\"AR\"", "\"Armenia\""), 1)
    report.top10CountriesToAirportsNum(3) shouldBe (Country("\"PN\"","\"Papua New Gunia\""), 0)

    report.countriesToSurfaces.size shouldBe 4
    report.countriesToSurfaces(0) shouldBe (Country("\"AR\"","\"Armenia\""), Seq("\"S01\""))
    report.countriesToSurfaces(1) shouldBe (Country("\"NI\"","\"Nicaragua\""), Seq())
    report.countriesToSurfaces(2) shouldBe (Country("\"PN\"","\"Papua New Gunia\""), Seq())
    report.countriesToSurfaces(3) shouldBe (Country("AG", "Argentina"), Seq("\"S01\"", "\"S02\""))

    report.top10MostCommonRunwayIdents.size shouldBe 3
    report.top10MostCommonRunwayIdents(0) shouldBe ("\"AA\"", 7)
    report.top10MostCommonRunwayIdents(1) shouldBe ("\"BB\"", 3)
    report.top10MostCommonRunwayIdents(2) shouldBe ("\"DD\"", 2)
  }
}
