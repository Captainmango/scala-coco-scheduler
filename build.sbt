val scala3Version = "3.3.8"

ThisBuild / semanticdbEnabled := true

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-coco-scheduler",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.4" % Test
    )
  )
