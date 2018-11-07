package sbtplayrestli

import buildinfo.BuildInfo
import play.sbt.PlayService
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

  override def requires = PlayService && RestliModelPlugin

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
