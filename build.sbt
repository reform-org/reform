import scala.sys.process
import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.ModuleInitializer

Global / onChangedBuildSource := ReloadOnSourceChanges

name                     := "Reform"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"
// ThisBuild / wartremoverErrors ++= Warts.unsafe

lazy val rescala = ProjectRef(file("REScala"), "rescalaJS")
lazy val rescala_kofre = ProjectRef(file("REScala"), "kofreJS")
lazy val loci_jsoniter = ProjectRef(file("scala-loci"), "lociSerializerJsoniterScalaJS")
lazy val loci_webrtc = ProjectRef(file("scala-loci"), "lociCommunicatorWebRtcJS")

lazy val webapp = project
  .dependsOn(rescala)
  .dependsOn(rescala_kofre)
  .dependsOn(loci_webrtc)
  .dependsOn(loci_jsoniter)
  .enablePlugins(
    ScalaJSPlugin
  )
  .settings(
    resolvers                              += "jitpack" at "https://jitpack.io",
    libraryDependencies                   ++= Seq(
      "io.github.outwatch"                    %%% "outwatch"                              % "1.0.0-RC12",
      "com.lihaoyi"                           %%% "utest"                                 % "0.8.1" % "test",
      "com.github.cornerman"                  %%% "colibri-router"                        % "0.7.1",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"                   % "2.17.9",
      "com.github.plokhotnyuk.jsoniter-scala"  %% "jsoniter-scala-macros"                 % "2.17.9",
    ),
    testFrameworks                         += new TestFramework("utest.runner.Framework"),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("webapp")))),
    scalacOptions ++= Seq("-no-indent"),
    Compile / scalaJSModuleInitializers    += {
      ModuleInitializer.mainMethod("webapp.Main", "main").withModuleID("main")
    },
    Test / scalaJSUseTestModuleInitializer := false,
    scalaJSLinkerConfig ~= { _.withOptimizer(false) },
  )
