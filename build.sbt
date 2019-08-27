val _scalaVersion = "2.11.11"

val akkaVersion = "2.5.11"
val akkaHttpVersion = "10.1.1"

val scalaJSDomVersion = "0.9.3"
val scalaJSReactVersion = "1.1.1"
val reactJSVersion = "15.6.1"

resolvers in ThisBuild += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

val httpDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

val root = (project in file("."))
  .settings(name := "countries-info",
    scalaVersion := _scalaVersion,
    scalaVersion in ThisBuild := _scalaVersion)

val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(name := "shared")

val sharedJVM = shared.jvm
val sharedJS = shared.js

val front = (Project("front", file("front"))
  dependsOn sharedJS
  enablePlugins ScalaJSPlugin
  settings(
    scalaJSUseMainModuleInitializer := true,
    scalaVersion := _scalaVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.play" %%% "play-json" % "2.6.9",
      "org.scala-js" %%% "scalajs-dom" % scalaJSDomVersion,
      "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion
    ),

    jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % reactJSVersion
      / "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % reactJSVersion
      / "react-dom.js"
      minified "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % reactJSVersion
      / "react-dom-server.js"
      minified "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer"
    )
  )
)

val back = (Project("back", file("back"))
  dependsOn sharedJVM
  settings(
    mainClass := Some("com.github.dedkovva.back.Boot"),
    (resources in Compile) ++= Seq(
      (fastOptJS in(front, Compile)).value.data,
      (packageJSDependencies in(front, Compile)).value
    ),
    (unmanagedResourceDirectories in Compile) <+= (baseDirectory in front) {
      _ / "src" / "main" / "resources"
    },

    libraryDependencies ++= (akkaDependencies ++ httpDependencies ++ Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.apache.spark" %% "spark-core" % "2.3.0"
    ))
  )
)