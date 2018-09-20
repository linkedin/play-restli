import com.linkedin.sbt._
import restli.All._

name         := "play-restli"
organization := "com.linkedin.pegasus"
version      := "0.1"
scalaVersion := "2.12.6"
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "d2",
  "restli-server",
  "restli-docgen",
  "restli-server-extras",
).map("com.linkedin.pegasus" % _ % "24.0.+")

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.2.+",
  "com.typesafe.akka" %% "akka-stream" % "2.5.+",
  "com.typesafe.play" %% "play" % "2.6.+",
  "com.typesafe.play" %% "play-java" % "2.6.+",
  "org.slf4j" % "slf4j-api" % "1.7.25",
)

libraryDependencies ++= Seq(
  "org.testng" % "testng" % "6.14.3" % Test,
  "org.easymock" % "easymock" % "3.6" % Test,
)

lazy val root = project in file(".")

val restliVersion = "24.0.+"

/**
  * This project is for hand written *.pdsc files.  It will generate "data template" class bindings into the
  * target/classes directory.
  */
lazy val dataTemplate = (project in file("example/data-template"))
  .compilePegasus()
  .settings(libraryDependencies += "com.linkedin.pegasus" % "data" % restliVersion)
  .settings(name := "data-template")


/**
  * This project contains your handwritten Rest.li "resource" implementations.  See rest.li documentation for detail
  * on how to write resource classes.
  */
lazy val server = (project in file("example/server"))
  .enablePlugins(PlayJava)
  .dependsOn(root, dataTemplate)
  .aggregate(dataTemplate, api)
  .settings(name := "server")
  .settings(libraryDependencies += "com.linkedin.pegasus" % "restli-server" % restliVersion)
  .settings(libraryDependencies += guice)
  .compileRestspec(
    apiName = "fortune",
    apiProject = api,
    resourcePackages = List("com.example.fortune"),
    dataTemplateProject = dataTemplate,
    compatMode = "ignore"
  )


/**
  * This project contains your API contract and will generate "client binding" classes into the
  * target/classes directory.  Clients to your rest.li service should depend on this project
  * or it's published artifacts (depend on the "restClient" configuration).
  *
  * Files under the src/idl and src/snapshot directories must be checked in to source control.  They are the
  * API contract and are used to generate client bindings and perform compatibility checking.
  */
lazy val api = (project in file("example/api"))
  .dependsOn(dataTemplate)
  .settings(name := "api")
  .settings(libraryDependencies += "com.linkedin.pegasus" % "restli-client" % restliVersion)
  .generateRequestBuilders(
    dataTemplateProject = dataTemplate
  )
