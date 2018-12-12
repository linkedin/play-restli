package com.linkedin.sbtplayrestli

import com.linkedin.sbtrestli.RestliModelPlugin
import com.linkedin.sbtrestli.RestliModelPlugin.autoImport._
import play.sbt.PlayLayoutPlugin
import sbt.Keys._
import sbt._

/** Generate rest models into target directory if using play layout. */
object PlayRestliLayoutPlugin extends AutoPlugin {
  object autoImport {
    val playRestliLayoutSettings: Seq[Def.Setting[_]] = Seq(
      target in restliModelGenerate := sourceManaged.value / "rest-model"
    )
  }

  import autoImport._

  override def requires = PlayLayoutPlugin && RestliModelPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(playRestliLayoutSettings) ++ inConfig(Test)(playRestliLayoutSettings)
}
