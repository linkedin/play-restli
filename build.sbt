val pegasusVersion = "24.0.2"
val playVersion = "2.6.19"

lazy val playRestli = (project in file("play-restli"))
  .settings(
    name         := "play-restli",
    organization := "com.linkedin.play-restli",
    version      := "0.1",
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      "com.google.inject" % "guice" % "4.2.2",
      "com.typesafe.akka" %% "akka-stream" % "2.5.16",
      "com.typesafe.play" %% "play" % playVersion,
      "com.typesafe.play" %% "play-java" % playVersion,
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.testng" % "testng" % "6.14.3" % Test,
      "org.easymock" % "easymock" % "3.6" % Test
    )
  )

lazy val api = (project in file("example/api"))
  .enablePlugins(RestliSchemaPlugin)
  .settings(
    name := "example-play-api",
    organization := "com.linkedin.play-restli",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "data" % pegasusVersion,
      "com.google.code.findbugs" % "jsr305" % "3.0.0"
    )
  )

lazy val commonServerSettings = Seq(
  restliModelApi := api,
  organization := "com.linkedin.play-restli",
  version := "0.1.0",
  buildInfoKeys += restliModelResourcePackages,
  buildInfoPackage := "sbtrestli",
  restliModelResourcePackages := Seq("com.example.fortune.impl"),
  libraryDependencies ++= Seq(
    "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
    guice
  )
)

// Uses the bare-bones PlayService plugin
lazy val playServiceServer = (project in file("example/server"))
  .enablePlugins(RestliModelPlugin, PlayService, BuildInfoPlugin)
  .dependsOn(api, playRestli)
  .settings(commonServerSettings)
  .settings(
    name := "example-play-service-server",
    libraryDependencies ++= Seq(
      akkaHttpServer,
      logback
    ),
    target := target.value / "play-service"
  )

// Uses the complete PlayJava plugin. PlayLayoutPlugin must be disabled in order to use the Restli project structure
lazy val playJavaServer = (project in file("example/server"))
  .enablePlugins(RestliModelPlugin, PlayJava, BuildInfoPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(api, playRestli)
  .settings(commonServerSettings)
  .settings(
    name := "example-play-java-server",
    target := target.value / "play-java"
  )
