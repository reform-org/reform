import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.ModuleInitializer

Global / onChangedBuildSource := ReloadOnSourceChanges

name                     := "Reform"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"
// ThisBuild / wartremoverErrors ++= Warts.unsafe

lazy val webapp = project
  .enablePlugins(
    ScalaJSPlugin,
  )
  .settings(
    resolvers                              += "jitpack" at "https://jitpack.io",
    libraryDependencies                   ++= Seq(
      "io.github.outwatch"                    %%% "outwatch"                              % "1.0.0-RC12",
      "com.github.rescala-lang.rescala"       %%% "rescala"                               % "bfe10f7ab2d79f13f0263677dffb90aec6d448c2",
      "com.lihaoyi"                           %%% "utest"                                 % "0.8.1" % "test",
      "com.github.cornerman"                  %%% "colibri-router"                        % "0.7.1",
      "com.github.scala-loci.scala-loci"      %%% s"scala-loci-serializer-jsoniter-scala" % "609b4c1b58",
      "com.github.scala-loci.scala-loci"      %%% s"scala-loci-communicator-webrtc"       % "609b4c1b58",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"                   % "2.17.9",
      "com.github.plokhotnyuk.jsoniter-scala"  %% "jsoniter-scala-macros"                 % "2.17.9",
    ),
    testFrameworks                         += new TestFramework("utest.runner.Framework"),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("webapp")))),
    Compile / scalaJSModuleInitializers    += {
      ModuleInitializer.mainMethod("webapp.Main", "main").withModuleID("main")
    },
    Test / scalaJSUseTestModuleInitializer := false,
    scalaJSLinkerConfig ~= { _.withOptimizer(false) },
  )
