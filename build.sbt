val scala3Version = "3.3.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-coco-scheduler",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.4" % Test
    )
  )
