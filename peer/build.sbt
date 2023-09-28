import scala.sys.process
import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.ModuleInitializer
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "Reform"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"
// ThisBuild / wartremoverErrors ++= Warts.unsafe

// https://stackoverflow.com/questions/33299892/how-to-depend-on-a-common-crossproject

lazy val reform = crossProject(JSPlatform, JVMPlatform)
  .in(file("src"))
  // .jsConfigure(_.enablePlugins(ScalablyTypedConverterExternalNpmPlugin))
  .jsSettings(
    Compile / scalaJSModuleInitializers := Seq({
      ModuleInitializer.mainMethod("de.tu_darmstadt.informatik.st.reform.Main", "main").withModuleID("main")
    }),
    Test / scalaJSUseTestModuleInitializer := true, // this disables the scalajsCom stuff (it injects some kind of communicator so the sbt test command works)
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory := target.value / "reform",
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory := target.value / "reform",
    libraryDependencies ++= Seq(
      "io.github.outwatch" %%% "outwatch" % "1.0.0-RC14",
      "com.github.cornerman" %%% "colibri-router" % "0.7.8",
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-webnative" % "3ea9afdeac1c46b5da65497b7d1fa54152128c2a",
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-webrtc" % "3ea9afdeac1c46b5da65497b7d1fa54152128c2a",
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-broadcastchannel" % "3ea9afdeac1c46b5da65497b7d1fa54152128c2a",
    ),
    /*
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
     */
  )
  .jvmSettings(
    fork := true,
    libraryDependencies ++= Seq(
      "com.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-jetty11" % "3ea9afdeac1c46b5da65497b7d1fa54152128c2a",
      "org.eclipse.jetty" % "jetty-slf4j-impl" % "11.0.14",
      "org.xerial" % "sqlite-jdbc" % "3.41.0.0",
      "com.auth0" % "java-jwt" % "4.3.0",
    ),
  )
  .settings(
    resolvers += "jitpack".at("https://jitpack.io"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.8.1" % Test,
      "com.github.scala-loci.scala-loci" %%% "scala-loci-serializer-jsoniter-scala" % "3ea9afdeac1c46b5da65497b7d1fa54152128c2a",
      "com.github.rescala-lang.REScala" %%% "rescala" % "7de346f7abbe81eb0cacd0ee7f49420a8ff527f7",
      "com.github.rescala-lang.REScala" %%% "kofre" % "7de346f7abbe81eb0cacd0ee7f49420a8ff527f7",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.21.2",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.21.2",
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq(
      "-no-indent",
      // "-W",
      // "-Y",
      "-Yexplicit-nulls",
      "-Ysafe-init",
      "-Wunused:all",
      "-deprecation",
      if (sys.env.get("CI").contains("true")) "-Werror" else "",
      // "-Xcheck-macros", // breaks utest, outwatch
    ),
  )

// needed by scalafix
ThisBuild / scalafixDependencies += "org.scalalint" %% "rules" % "0.1.4"
inThisBuild(
  List(
    scalaVersion := "3.3.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  ),
)
