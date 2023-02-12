import scala.sys.process
import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.ModuleInitializer
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "Reform"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0-RC2"
ThisBuild / fork := true
// ThisBuild / wartremoverErrors ++= Warts.unsafe

// https://stackoverflow.com/questions/33299892/how-to-depend-on-a-common-crossproject

lazy val rescalaJS = ProjectRef(file("REScala"), "rescalaJS")
lazy val rescalaJVM = ProjectRef(file("REScala"), "rescalaJVM")
lazy val kofreJS = ProjectRef(file("REScala"), "kofreJS")
lazy val kofreJVM = ProjectRef(file("REScala"), "kofreJVM")

lazy val webapp = crossProject(JSPlatform, JVMPlatform)
  // .jsConfigure(_.dependsOn(rescalaJS).dependsOn(kofreJS))
  // .jvmConfigure(_.dependsOn(rescalaJVM).dependsOn(kofreJVM))
  .in(file("."))
  .jsConfigure(_.enablePlugins(ScalablyTypedConverterExternalNpmPlugin))
  .jsSettings(
    Compile / scalaJSModuleInitializers := Seq({
      ModuleInitializer.mainMethod("webapp.Main", "main").withModuleID("main")
    }),
    Test / scalaJSUseTestModuleInitializer := true, // this disables the scalajsCom stuff (it injects some kind of communicator so the sbt test command works)
    /*Test / scalaJSModuleInitializers := Seq(
      {ModuleInitializer.mainMethod("webapp.MainJSTest", "main").withModuleID("main")}
    ),*/
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("webapp")))),
    scalaJSLinkerConfig ~= { _.withOptimizer(false) },
    libraryDependencies ++= Seq(
      "io.github.outwatch" %%% "outwatch" % "1.0.0-RC14",
      "com.github.cornerman" %%% "colibri-router" % "0.7.8",
      "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1",
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-webnative" % "69ab30877539712051f508bdf680134e90032e0b",
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-webrtc" % "69ab30877539712051f508bdf680134e90032e0b",
    ),
    externalNpm := baseDirectory.value.getParentFile(),
    stIgnore := List(
      "@types/chance",
      "@types/selenium-webdriver",
      "browserstack-local",
      "chance",
      "daisyui",
      "pdf-lib",
      "selenium-webdriver",
      "snabbdom",
      "typescript",
    ),
    stStdlib := List("esnext", "dom"),
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-jetty11" % "69ab30877539712051f508bdf680134e90032e0b",
    ),
  )
  .settings(
    resolvers += "jitpack".at("https://jitpack.io"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.8.1" % Test,
      "com.github.scala-loci.scala-loci" %%% "scala-loci-serializer-jsoniter-scala" % "69ab30877539712051f508bdf680134e90032e0b",
      "com.github.rescala-lang.REScala" %%% "rescala" % "70dc2dde6cdc93abb9aaaa39fe8b6983f381b899",
      "com.github.rescala-lang.REScala" %%% "kofre" % "70dc2dde6cdc93abb9aaaa39fe8b6983f381b899",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.20.6",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.20.6",
    ),
    libraryDependencies += compilerPlugin("com.github.ghik" % "zerowaste" % "0.2.4" cross CrossVersion.full),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq(
      "-no-indent",
      // "-W",
      // "-Y",
      // "-Yexplicit-nulls", // breaks json macro, probably also coverage
      "-Ysafe-init",
      "-Wunused:all",
      "-Wvalue-discard",
      "-deprecation",
      if (sys.env.get("CI") == Some("true")) "-Werror" else "",
      // "-Xcheck-macros", // breaks utest, outwatch
    ),
  )
