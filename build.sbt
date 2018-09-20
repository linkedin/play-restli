name         := "play-restli"
organization := "com.linkedin.pegasus"
version      := "0.1"
scalaVersion := "2.12.6"

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
)

libraryDependencies ++= Seq(
  "org.testng" % "testng" % "6.14.3" % Test,
  "org.easymock" % "easymock" % "3.6" % Test,
)

lazy val root = project in file(".")
