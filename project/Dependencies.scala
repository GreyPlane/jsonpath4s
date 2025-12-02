package jsonpath4s

import sbt.Keys.*
import sbt.*
import org.portablescala.sbtplatformdeps.*

object Dependencies {

  import PlatformDepsPlugin.autoImport.*

  val Scala213Version        = "2.13.17"
  val Scala3Version          = "3.7.4"
  val SupportedScalaVersions = Seq(Scala213Version, Scala3Version)

  object Versions {
    val munit           = "1.2.1"
    val parboiled       = "2.5.1"
    val cats            = "2.13.0"
    val monocle         = "3.3.0"
    val circe           = "0.14.15"
    val circeOptics     = "0.15.0"
    val sprayJson       = "1.3.6"
    val sprayJsonOptics = "0.1.7"
  }

  object Compile {
    val sprayJson       = "io.spray"            %% "spray-json"        % Versions.sprayJson
    val sprayJsonOptics = "io.github.greyplane" %% "spray-json-optics" % Versions.sprayJsonOptics
  }

  object Test {
    val munit       = "org.scalameta" %% "munit"        % Versions.munit % sbt.Test
    val circeParser = "io.circe"      %% "circe-parser" % Versions.circe % sbt.Test
  }

  private val deps = libraryDependencies

  val core = deps ++= Seq(
    "org.parboiled" %%% "parboiled" % Versions.parboiled
  )

  val coreTest = deps ++= Seq(
    Test.munit
  )

  val optics = deps ++= Seq(
    "org.typelevel" %%% "cats-core"    % Versions.cats,
    "dev.optics"    %%% "monocle-core" % Versions.monocle
  )

  val circe = deps ++= Seq(
    "io.circe" %%% "circe-core"   % Versions.circe,
    "io.circe" %%% "circe-optics" % Versions.circeOptics
  )

  val circeTest = deps ++= Seq(
    Test.circeParser,
    Test.munit
  )

  val spray = deps ++= Seq(
    Compile.sprayJson,
    Compile.sprayJsonOptics
  )

  val sprayTest = deps ++= Seq(
    Test.munit
  )

}
