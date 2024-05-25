ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

lazy val core = (project in file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "org.parboiled" %% "parboiled" % "2.5.1",
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.scalameta" %% "munit"     % "1.0.0" % Test
    )
  )

lazy val optics = (project in file("optics"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.optics" %% "monocle-core" % "3.2.0"
    )
  )
  .dependsOn(core)

lazy val circe = (project in file("circe"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe"      %% "circe-core"   % "0.14.7",
      "io.circe"      %% "circe-optics" % "0.15.0",
      "io.circe"      %% "circe-parser" % "0.14.7" % Test,
      "org.scalameta" %% "munit"        % "1.0.0"  % Test
    )
  )
  .dependsOn(optics)

lazy val root = (project in file("."))
  .settings(
    name := "jsonpath4s"
  )
  .aggregate(core, optics, circe)
