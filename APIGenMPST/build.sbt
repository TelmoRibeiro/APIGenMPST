lazy val APIGenMPSTBuild = project
  .in(file("."))
  // .enablePlugins(ScalaJSPlugin) // uncomment me
  .settings(
    name := "APIGenMPST",
    scalaVersion :=  "3.4.0" ,
    // scalaJSUseMainModuleInitializer := true,
    // libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.3.0",
  )