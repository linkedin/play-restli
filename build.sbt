val pegasusVersion = "24.0.2"
val playVersion = "2.6.20"

ThisBuild / organization := "com.linkedin.play-restli"
ThisBuild / version      := "0.1.0-SNAPSHOT"


lazy val playRestli = (project in file("play-restli"))
  .settings(
    name := "play-restli",
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    libraryDependencies ++= Seq(
      "com.linkedin.pegasus" % "restli-server" % pegasusVersion,
      "com.typesafe.play" %% "play" % playVersion,
      "com.typesafe.play" %% "play-java" % playVersion,
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "junit" % "junit" % "4.12" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "com.tngtech.junit.dataprovider" % "junit4-dataprovider" % "2.4" % Test,
      "org.easymock" % "easymock" % "3.6" % Test
    ),
    // Needed in order to run junit tests
    // https://github.com/sbt/junit-interface/issues/35
    crossPaths in Test := false
  )

lazy val sbtPlayRestli = (project in file("sbt-plugin"))
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-plugin",
    crossSbtVersions := Seq("0.13.17", "1.2.6"),
    crossScalaVersions := Seq("2.10.7", "2.12.7"),
    buildInfoKeys += organization,
    scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value),
    scriptedDependencies := scriptedDependencies.dependsOn(publishLocal in playRestli).value,
    addSbtPlugin("com.linkedin.sbt-restli" % "sbt-restli" % "0.3.9" % Provided),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion % Provided)
  )
