import jsonpath4s.Dependencies

import sbt.Keys.*

val buildSettings = List(
  scalacOptions ++= Seq("-Wall"),
  scalaVersion             := Dependencies.Scala3Version,
  tlBaseVersion            := "0.0",
  tlCiHeaderCheck          := false,
  tlCiMimaBinaryIssueCheck := false,
  tlMimaPreviousVersions   := Set.empty,
  tlCiDocCheck             := false,
  tlCiReleaseBranches      := Seq("master"),
  tlCiDependencyGraphJob   := false
)

val publishSettings = List(
  organization := "io.github.greyplane",
  homepage     := Some(url("https://github.com/GreyPlane/jsonpath4s")),
  licenses     := List(sbt.librarymanagement.License.MIT),
  developers   := List(Developer("GreyPlane", "Liu Ji", "greyplane@gmail.com", url("https://github.com/GreyPlane")))
)

inThisBuild(buildSettings ++ publishSettings)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(moduleName := "jsonpath4s-core", name := "JsonPath4s Core")
  .settings(Dependencies.core)
  .settings(Dependencies.coreTest)

lazy val optics = crossProject(JVMPlatform, JSPlatform)
  .in(file("optics"))
  .settings(moduleName := "jsonpath4s-optics", name := "JsonPath4s Optics")
  .settings(Dependencies.optics)
  .dependsOn(core)

lazy val circe = crossProject(JVMPlatform, JSPlatform)
  .in(file("circe"))
  .settings(moduleName := "jsonpath4s-circe", name := "JsonPath4s Circe")
  .settings(Dependencies.circe)
  .settings(Dependencies.circeTest)
  .dependsOn(optics)

lazy val spray = (project in file("spray"))
  .settings(moduleName := "jsonpath4s-spray-json", name := "JsonPath4s Spray Json")
  .settings(Dependencies.spray)
  .settings(Dependencies.sprayTest)
  .dependsOn(optics.jvm)

lazy val root = tlCrossRootProject
  .aggregate(core, optics, circe, spray)
