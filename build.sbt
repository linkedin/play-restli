val pegasusVersion = "24.0.2"
val playVersion = "2.6.20"

ThisBuild / organization := "com.linkedin.play-restli"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"


lazy val playRestli = (project in file("play-restli"))
  .settings(
    name := "play-restli",
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      "com.typesafe.akka" %% "akka-stream" % "2.5.16",
      "com.typesafe.play" %% "play" % playVersion,
      "com.typesafe.play" %% "play-java" % playVersion,
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.testng" % "testng" % "6.14.3" % Test,
      "org.easymock" % "easymock" % "3.6" % Test
    )
  )

lazy val sbtPlayRestli = (project in file("sbt-plugin"))
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-plugin",
    crossSbtVersions := Seq("1.2.6", "0.13.17"),
    buildInfoKeys += organization,
    scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value),
    scriptedDependencies := scriptedDependencies.dependsOn(publishLocal in playRestli).value,
    addSbtPlugin("com.linkedin.sbt-restli" % "sbt-restli" % "0.3.9" % Provided),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion % Provided)
  )
