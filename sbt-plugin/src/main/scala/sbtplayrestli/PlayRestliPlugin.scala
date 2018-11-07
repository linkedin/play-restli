package sbtplayrestli

import buildinfo.BuildInfo
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import sbt.Keys._
import sbt._
import sbtrestli.RestliModelPlugin
import sbtrestli.RestliModelPlugin.autoImport._

object PlayRestliPlugin extends AutoPlugin {
  object autoImport {
    val playRestliDependency = BuildInfo.organization %% "play-restli" % BuildInfo.version
    val playRestliSettings: Seq[Def.Setting[_]] = Seq(
      sourceGenerators += Def.task {
        val file = sourceManaged.value / "play-restli" / "PlayRestli.java"
        val resourcePackages = restliModelResourcePackages.value

        generateSource(file, resourcePackages)

        Seq(file)
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

  private def generateSource(file: File, resourcePackages: Seq[String]): Unit = {
    val source =
      s"""|public class PlayRestli {
          |  public static final String[] resourcePackages = ${resourcePackages.mkString("{ \"", "\", \"", "\" };")}
          |}""".stripMargin

    IO.write(file, source)
  }
}
