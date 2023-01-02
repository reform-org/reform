import scala.sys.process
import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.ModuleInitializer
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

Global / onChangedBuildSource := ReloadOnSourceChanges

name                     := "Reform"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"
// ThisBuild / wartremoverErrors ++= Warts.unsafe

// https://stackoverflow.com/questions/33299892/how-to-depend-on-a-common-crossproject

lazy val rescalaJS = ProjectRef(file("REScala"), "rescalaJS")
lazy val rescalaJVM = ProjectRef(file("REScala"), "rescalaJVM")
lazy val kofreJS = ProjectRef(file("REScala"), "kofreJS")
lazy val kofreJVM = ProjectRef(file("REScala"), "kofreJVM")

lazy val webapp = crossProject(JSPlatform, JVMPlatform)
  //.jsConfigure(_.dependsOn(rescalaJS).dependsOn(kofreJS))
  //.jvmConfigure(_.dependsOn(rescalaJVM).dependsOn(kofreJVM))
  .in(file("."))
  .jsSettings(
    Compile / scalaJSModuleInitializers    := Seq({
      ModuleInitializer.mainMethod("webapp.Main", "main").withModuleID("main")
    }),
    Test / scalaJSUseTestModuleInitializer := false, // this disables the scalajsCom stuff (it injects some kind of communicator so the sbt test command works)
    Test / scalaJSModuleInitializers := Seq(
      //{ModuleInitializer.mainMethod("webapp.MainTest", "main").withModuleID("main")}
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("webapp")))),
    scalaJSLinkerConfig ~= { _.withOptimizer(false) },
    libraryDependencies                   ++= Seq(
      "io.github.outwatch"                    %%% "outwatch"                              % "1.0.0-RC13",
      "com.github.cornerman"                  %%% "colibri-router"                        % "0.7.8"
    )
  )
  .settings(
    resolvers                              += "jitpack" at "https://jitpack.io",
    libraryDependencies                   ++= Seq(
      "com.lihaoyi"                           %%% "utest"                                 % "0.8.1" % "test",
      "com.github.scala-loci.scala-loci"      %%% "scala-loci-serializer-jsoniter-scala"  % "5df6d12a45d67732763ecab05983484517a926d7",
      "com.github.scala-loci.scala-loci"      %%% "scala-loci-communicator-webrtc"        % "5df6d12a45d67732763ecab05983484517a926d7",
      "com.github.rescala-lang.rescala"       %%% "rescala"                               % "e522b921dec85ba1f45356dba3849c0577273260",
      "com.github.rescala-lang.rescala"       %%% "kofre"                                 % "e522b921dec85ba1f45356dba3849c0577273260",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"                   % "2.20.1",
      "com.github.plokhotnyuk.jsoniter-scala"  %% "jsoniter-scala-macros"                 % "2.20.1"
    ),
    testFrameworks                         += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq(
      // like there could also be sane defaults but no
      //"-rewrite",
      "-no-indent",
      //"-Yexplicit-nulls", // breaks json macro
      "-Ysafe-init",
      "-Xfatal-warnings",
      "--unchecked",
      "-deprecation",
      "-Xmigration",
      "-Wunused:all",
      "-explain"
      //"-Xcheck-macros" // breaks utest
    )
  )
