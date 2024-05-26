import jsonpath4s.Dependencies

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

lazy val core = (project in file("core"))
  .settings(Dependencies.core)
  .settings(Dependencies.coreTest)

lazy val optics = (project in file("optics"))
  .settings(Dependencies.optics)
  .dependsOn(core)

lazy val circe = (project in file("circe"))
  .settings(Dependencies.circe)
  .settings(Dependencies.circeTest)
  .dependsOn(optics)

lazy val spray = (project in file("spray"))
  .settings(Dependencies.spray)
  .settings(Dependencies.sprayTest)
  .dependsOn(optics)

lazy val root = (project in file("."))
  .settings(
    name := "jsonpath4s"
  )
  .aggregate(core, optics, circe, spray)
