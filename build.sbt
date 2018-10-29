val pegasusVersion = "24.0.+"

lazy val playRestli = (project in file("play-restli"))
  .settings(
    name         := "play-restli",
    organization := "com.linkedin.pegasus",
    version      := "0.1",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "d2",
      "restli-server",
      "restli-docgen",
      "restli-server-extras"
    ).map("com.linkedin.pegasus" % _ % pegasusVersion),
    libraryDependencies ++= Seq(
      "com.google.inject" % "guice" % "4.2.+",
      "com.typesafe.akka" %% "akka-stream" % "2.5.+",
      "com.typesafe.play" %% "play" % "2.6.+",
      "com.typesafe.play" %% "play-java" % "2.6.+",
      "org.slf4j" % "slf4j-api" % "1.7.25"
    ),
    libraryDependencies ++= Seq(
      "org.testng" % "testng" % "6.14.3" % Test,
      "org.easymock" % "easymock" % "3.6" % Test
    )
  )

lazy val api = (project in file("example/api"))
  .enablePlugins(RestliSchemaPlugin)
  .settings(
    name := "example-play-api",
    organization := "com.linkedin.pegasus",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "data" % pegasusVersion,
      "com.google.code.findbugs" % "jsr305" % "3.0.0"
    )
  )

lazy val server = (project in file("example/server"))
  .enablePlugins(RestliModelPlugin, PlayService)
  .dependsOn(api, playRestli)
  .settings(
    restliModelApi := api,
    name := "example-play-server",
    organization := "com.linkedin.pegasus",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      guice,
      akkaHttpServer,
      logback
    )
  )
