val pegasusVersion = "24.0.2"
val playVersion = "2.6.22"

val repoUrl = url("https://github.com/linkedin/play-restli")

inThisBuild(Seq(
  licenses := Seq(("BSD Simplified", url("https://github.com/linkedin/play-restli/blob/master/LICENSE"))),
  homepage := Some(repoUrl),
  scmInfo := Some(ScmInfo(repoUrl, "scm:git:git@github.com:linkedin/play-restli.git")),
  developers := List(Developer("TylerHorth", "Tyler Horth", "tylerhorth@outlook.com", url("https://github.com/TylerHorth"))),
  organization := "com.linkedin.play-restli",
  pgpPublicRing := file("./travis/local.pubring.asc"),
  pgpSecretRing := file("./travis/local.secring.asc"),
  resolvers += Resolver.sonatypeRepo("releases")
))

lazy val playRestli = (project in file("play-restli"))
  .settings(
    name := "play-restli",
    crossScalaVersions := Seq("2.11.12", "2.12.7"),
    releaseEarlyWith := SonatypePublisher,
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

lazy val sbtPlayRestli = (project in file("sbt-play-restli"))
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-play-restli",
    crossSbtVersions := Seq("0.13.17", "1.2.6"),
    crossScalaVersions := Seq("2.10.7", "2.12.7"),
    buildInfoKeys += organization,
    scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value),
    scriptedDependencies := scriptedDependencies.dependsOn(publishLocal in playRestli).value,
    releaseEarlyEnableSyncToMaven := false,
    bintrayOrganization := Some("play-restli"),
    releaseEarlyWith := BintrayPublisher,
    addSbtPlugin("com.linkedin.sbt-restli" % "sbt-restli" % "0.3.9" % Provided),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion % Provided)
  )
