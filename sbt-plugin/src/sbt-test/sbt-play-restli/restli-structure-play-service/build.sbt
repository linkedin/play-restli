val pegasusVersion = "24.0.2"

lazy val api = (project in file("api"))
  .enablePlugins(RestliSchemaPlugin)
  .settings(
    name := "api",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "data" % pegasusVersion,
      "com.google.code.findbugs" % "jsr305" % "3.0.0"
    )
  )

// Uses the bare-bones PlayService plugin
lazy val server = (project in file("server"))
  .enablePlugins(RestliModelPlugin, PlayService)
  .dependsOn(api)
  .settings(
    name := "server",
    scalaVersion := "2.12.7",
    restliModelApi := api,
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      guice, akkaHttpServer, logback
    )
  )