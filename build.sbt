Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

// https://docs.scala-lang.org/scala3/guides/migration/tutorial-sbt.html
name                     := "Reform"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"
ThisBuild / wartremoverErrors ++= Warts.unsafe

val versions = new {
  val outwatch  = "1.0.0-RC12"
  val funPack   = "file:./../../../../../fun-pack"
  val scalaTest = "3.2.14"
}

lazy val scalaJsMacrotaskExecutor = Seq(
  // https://github.com/scala-js/scala-js-macrotask-executor
  libraryDependencies       += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.0",
  Compile / npmDependencies += "setimmediate"  -> "1.0.5", // polyfill
)

lazy val webapp = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .settings(scalaJsMacrotaskExecutor)
  .settings(
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies          ++= Seq(
      "io.github.outwatch" %%% "outwatch"  % versions.outwatch,
      "com.github.rescala-lang.rescala" %%% "rescala" % "bfe10f7ab2d79f13f0263677dffb90aec6d448c2",
      "org.scalatest"      %%% "scalatest" % versions.scalaTest % Test,
    ),
    Compile / npmDevDependencies ++= Seq(
      "@fun-stack/fun-pack" -> versions.funPack, // sane defaults for webpack development and production, see webpack.config.*.js
    ),
    scalacOptions --= Seq(
      "-Xfatal-warnings",
    ), // overwrite option from https://github.com/DavidGregory084/sbt-tpolecat

    scalaJSLinkerConfig ~= (_.withModuleKind(
      ModuleKind.CommonJSModule,
    )), // configure Scala.js to emit a JavaScript module instead of a top-level script
    scalaJSUseMainModuleInitializer   := true, // On Startup, call the main function
    webpackDevServerPort              := 12345,
    webpack / version := "5.75.0",
    startWebpackDevServer / version := "4.11.1",
    webpackCliVersion := "4.10.0",
    webpackDevServerExtraArgs         := Seq("--color"),
    fullOptJS / webpackEmitSourceMaps := true,
    fastOptJS / webpackBundlingMode   := BundlingMode
      .LibraryOnly(), // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.prod.js"),
    Test / requireJsDomEnv        := true,
  )

addCommandAlias("prod", "fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("devInit", "; webapp/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; webapp/fastOptJS/webpack")
addCommandAlias("devDestroy", "webapp/fastOptJS/stopWebpackDevServer")
