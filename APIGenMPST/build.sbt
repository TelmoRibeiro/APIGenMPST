import javax.naming.spi.Resolver

ThisBuild / scalaVersion := "3.4.1"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "APIGenMPST",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.3.0",
    // libraryDependencies += "org.playframework" %% "play-json" % "3.0.2",
    scalaJSUseMainModuleInitializer := true,
  ).enablePlugins(ScalaJSPlugin) // comment me!