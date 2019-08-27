package com.github.dedkovva.back

import com.github.dedkovva.shared._
import org.apache.spark.rdd.RDD

import scala.io.Source
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * Service to get information from `csv-files countries.csv, airports.csv, runways.csv` in `resources/task-data folder`.
  *
  * <ul>It filters items from files by trimmed non-empty fields:</br>
  * <li>countries by `"code"`</li>
  * <li>airports by `"id"` and `"iso_country"`</li>
  * <li>runways by `"id"` and `"airport_ref"`</li>
  * </ul>
  *
  * Created by dedkov-va on 07.04.18.
  */
object Service {
  private val logger = LoggerFactory.getLogger(Service.getClass)
  logger.info("service starting ...")

  private val taskDataFolder = "task-data"

  @transient private lazy val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("countries-info")
  @transient private lazy val sc: SparkContext = new SparkContext(conf)

  private def t(line: Array[String], index: Int) = if (line.length > index) line(index) else ""

  private def prepareFile(fileName: String): (List[String], List[String]) = {
    val lines = Source.fromFile(getClass.getClassLoader.getResource(fileName).toURI, "UTF-8").getLines().toList
    (lines.tail, lines.head.split(",").toList)
  }
  
  private def toList[T](lines: List[String], lineToClass: Array[String] => T): List[T] = {
    for {
      line <- lines
      l = line.split(",")
    } yield lineToClass(l)
  }

  private val countries: RDD[(String, Country)] = {
    val (lines, header) = prepareFile(s"$taskDataFolder/countries.csv")
    val codeIndex = header.indexOf(s""""code"""")
    val nameIndex = header.indexOf(s""""name"""")
    val countries = toList(lines, l => Country(code = t(l, codeIndex), name = t(l, nameIndex)))
    sc.parallelize(countries).filter(_.code.trim.nonEmpty).keyBy(_.code).cache()
  }

  private val airports: RDD[(String, Airport)] = {
    val (lines, header) = prepareFile(s"$taskDataFolder/airports.csv")
    val idIndex = header.indexOf(s""""id"""")
    val nameIndex = header.indexOf(s""""name"""")
    val iataIndex = header.indexOf(s""""iata_code"""")
    val countryCodeIndex = header.indexOf(s""""iso_country"""")
    val airports = toList(lines, l => Airport(
      id = t(l, idIndex), name = t(l, nameIndex), iataCode = t(l, iataIndex), countryCode = t(l, countryCodeIndex)))
    sc.parallelize(airports).filter(e => e.id.trim.nonEmpty && e.countryCode.trim.nonEmpty)
      .keyBy(_.countryCode).cache()
  }

  private val runways: RDD[(String, Runway)] = {
    val (lines, header) = prepareFile(s"$taskDataFolder/runways.csv")
    val idIndex = header.indexOf(s""""id"""")
    val airportIdIndex = header.indexOf(s""""airport_ref"""")
    val surfaceIndex = header.indexOf(s""""surface"""")
    val identIndex = header.indexOf(s""""le_ident"""")
    val runways = toList(lines, l => Runway(
      id = t(l, idIndex), airportId = t(l, airportIdIndex), surface = t(l, surfaceIndex), ident = t(l, identIndex)))
    sc.parallelize(runways).filter(e => e.id.trim.nonEmpty && e.airportId.trim.nonEmpty)
      .keyBy(_.airportId).cache()
  }

  private val joined: RDD[(Country, Option[Airport], Option[Runway])] = {
    val joined1 = countries.leftOuterJoin(airports).map((e: (String, (Country, Option[Airport]))) =>
      (e._2._2.map(_.id).getOrElse(""), e._2))
    joined1.leftOuterJoin(runways).map((e: (String, ((Country, Option[Airport]), Option[Runway]))) =>
      (e._2._1._1, e._2._1._2, e._2._2)).cache()
  }

  private val countriesToAirportNum: List[(Country, Int)] = {
    val countryToAirportNum = countries.leftOuterJoin(airports).map(_._2).groupByKey
      .map((e: (Country, Iterable[Option[Airport]])) => (e._1, e._2.flatten.count(_ => true)))
    countryToAirportNum.collect().sortBy(e => (e._2, e._1.code)).toList
  }

  /**
    * Report about countries, airports and runways.
    * Calculated only once at service initialization.
    */
  protected[back] val report: Report = evalReport()

  logger.info("service started")

  /**
    * Returns countries with joined airports with joined runways.<br/>
    *
    * It uses left joins, so you can see countries without airports and/or airports without runways.<br/>
    * It searches countries by `"name"` or `"code"` fields containing `name` parameter.<br/>
    * It searches by ignoring letter cases.
    *
    * @param name string to find
    * */
  protected[back] def query(name: String): Seq[(Country, Seq[(Airport, Seq[Runway])])] = {
    val upName = name.toUpperCase
    val grouped = joined.filter((e: (Country, Option[Airport], Option[Runway])) =>
      e._1.code.toUpperCase.contains(upName) || e._1.name.toUpperCase.contains(upName))
      .groupBy((e: (Country, Option[Airport], Option[Runway])) => (e._1, e._2))
      .groupBy((e: ((Country, Option[Airport]), Iterable[(Country, Option[Airport], Option[Runway])])) => e._1._1)
      .map((e: (Country, Iterable[((Country, Option[Airport]), Iterable[(Country, Option[Airport], Option[Runway])])])) =>
        (e._1, e._2.map(e => (e._1._2, e._2.map(e => e._3)))))
    val collected = grouped.collect().toList.map(e => (e._1, e._2.toList.map(e => (e._1, e._2.toList))))
    val flattened = collected.map((e: (Country, List[(Option[Airport], List[Option[Runway]])])) =>
      (e._1, e._2.filter(_._1.nonEmpty).map(e => (e._1.get, e._2.flatten))))
    val sorted = flattened.sortBy(_._1.code).map(e => (e._1, e._2.sortBy(_._1.id).map(e => (e._1, e._2.sortBy(_.id)))))
    sorted
  }

  private def evalBottom10CountriesToAirportsNum() = countriesToAirportNum.take(10)
  private def evalTop10CountriesToAirportsNum() = countriesToAirportNum.reverse.take(10)
  private def evalCountriesToSurfaces() = {
    val grouped = joined.map((e: (Country, Option[Airport], Option[Runway])) =>
      (e._1, e._3.map(_.surface))).groupByKey().map((e: (Country, Iterable[Option[String]])) => (e._1, e._2.flatten))
    grouped.collect().toList.map(e => (e._1, e._2.toSet.toList.sorted)).sortBy(_._1.code)
  }
  private def evalTop10MostCommonRunwayIdents() =
    runways.map(_._2.ident).map(e => (e, 1L)).reduceByKey(_ + _).collect().toList.sortBy(e => (e._2, e._1)).reverse
      .take(10)

  private def evalReport(): Report = {
    Report(
      evalBottom10CountriesToAirportsNum(),
      evalTop10CountriesToAirportsNum(),
      evalCountriesToSurfaces(),
      evalTop10MostCommonRunwayIdents())
  }
}
