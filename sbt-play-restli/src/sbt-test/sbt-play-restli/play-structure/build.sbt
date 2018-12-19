val pegasusVersion = "24.0.2"

inThisBuild(
  scalaVersion := "2.12.7"
)

lazy val api = (project in file("api"))
  .enablePlugins(RestliSchemaPlugin)
  .settings(
    name := "api",
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "data" % pegasusVersion,
      "com.google.code.findbugs" % "jsr305" % "3.0.0"
    )
  )

lazy val client = (project in file("client"))
  .enablePlugins(RestliClientPlugin)
  .dependsOn(api)
  .settings(
    name := "client",
    restliClientApi := api,
    libraryDependencies += "com.linkedin.pegasus" % "restli-client" % pegasusVersion
  )

// Uses the complete PlayJava plugin, including PlayLayoutPlugin.
lazy val server = (project in file("server"))
  .enablePlugins(RestliModelPlugin, PlayJava)
  .dependsOn(api, client % Test)
  .settings(
    name := "server",
    restliModelApi := api,
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      guice
    )
  )
