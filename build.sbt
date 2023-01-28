import higherkindness.mu.rpc.srcgen.Model._

inThisBuild(Seq(
  organization := "com.ricopinazo",
  scalaVersion := "2.13.10",
  scalacOptions += "-language:higherKinds"
))

def on[A](major: Int, minor: Int)(a: A): Def.Initialize[Seq[A]] =
  Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some(v) if v == (major, minor) => Seq(a)
      case _                              => Nil
    }
  }

lazy val macroSettings: Seq[Setting[_]] = Seq(
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided
  ),
  libraryDependencies ++= on(2, 12)(
    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full)
  ).value,
  scalacOptions ++= on(2, 13)("-Ymacro-annotations").value
)

val muVersion = "0.30.2"

val root = (project in file("."))
  .settings(
    name := "grpc-health-check-example",
    libraryDependencies ++= Seq(
      // Needed to import protobuf packages
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      // Needed for the generated code to compile
      "io.higherkindness" %% "mu-rpc-service" % muVersion,
      // Needed to build a gRPC server
      "io.higherkindness" %% "mu-rpc-server" % muVersion,
      // Needed to build a gRPC client (although you could use mu-rpc-okhttp instead)
      "io.higherkindness" %% "mu-rpc-client-netty" % muVersion,
      "io.higherkindness" %% "mu-rpc-health-check" % muVersion,
      // Silence all logs in the demo
      "org.slf4j" % "slf4j-nop" % "1.7.30",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    ),
    // Needed to expand the @service macro annotation
    macroSettings,
    // Generate sources from .proto files
    muSrcGenIdlType := IdlType.Proto,
    // Make it easy for 3rd-party clients to communicate with us via gRPC
    muSrcGenIdiomaticEndpoints := true,
    // Start the server in a separate process so it shuts down cleanly when you hit Ctrl-C
    fork := true
  )
  .enablePlugins(SrcGenPlugin)
