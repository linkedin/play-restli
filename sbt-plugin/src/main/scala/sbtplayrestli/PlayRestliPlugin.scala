package sbtplayrestli

import java.net.URLClassLoader

import buildinfo.BuildInfo
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.config.ConfigFactory
import sbt.Keys._
import sbt._
import sbtrestli.RestliModelPlugin
import sbtrestli.RestliModelPlugin.autoImport._

import scala.collection.JavaConverters._

object PlayRestliPlugin extends AutoPlugin {
  object autoImport {
    val playRestliDependency = BuildInfo.organization %% "play-restli" % BuildInfo.version
    val playRestliSettings: Seq[Def.Setting[_]] = Seq(
      restliModelResourcePackages := {
        restliModelResourcePackages.?.value.getOrElse {
          val resourceUrls = resources.value.map(_.toURI.toURL).toArray
          val loader = new URLClassLoader(resourceUrls, null)
          val config = ConfigFactory.defaultApplication(loader)

          config.getStringList("play.restli.resourcePackages").asScala
        }
      }
    )
  }

  import autoImport._

  // JavaServerAppPackaging is the closest common ancestor of PlayJava and PlayService.
  // Starting with Play 2.7.x PlayJava will use PlayService as a base plugin and JavaServerAppPackaging can be replaced.
  override def requires = RestliModelPlugin && JavaServerAppPackaging
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(playRestliSettings) ++ inConfig(Test)(playRestliSettings) ++ Seq(
      libraryDependencies += playRestliDependency
    )
}
