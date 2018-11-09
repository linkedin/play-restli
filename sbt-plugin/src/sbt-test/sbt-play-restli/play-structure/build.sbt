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

lazy val clientBindings = (project in file("api"))
  .enablePlugins(RestliClientPlugin)
  .dependsOn(api)
  .settings(
    name := "client",
    scalaVersion := "2.12.7",
    libraryDependencies += "com.linkedin.pegasus" % "restli-client" % "24.0.2",
    target := target.value / "client"
  )

// Uses the complete PlayJava plugin, including PlayLayoutPlugin.
lazy val server = (project in file("server"))
  .enablePlugins(RestliModelPlugin, PlayJava)
  .dependsOn(api, clientBindings % Test)
  .settings(
    name := "server",
    scalaVersion := "2.12.7",
    restliModelApi := api,
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      guice
    )
  )
