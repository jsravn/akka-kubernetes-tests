import Dependencies._
import com.typesafe.sbt.packager.docker._

// To be compatible with Docker tags
version in ThisBuild ~= (_.replace('+', '-'))

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "com.lightbend.akka",
    name := "akka-kubernetes",
    organizationName := "Lightbend Inc.",
    startYear := Some(2018),
    licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Xfuture"
    ),
    headerLicense := Some(
      HeaderLicense.Custom(
        """Copyright (C) 2018 Lightbend Inc. <http://www.lightbend.com>"""
      )
    ),
    resolvers += Resolver.bintrayRepo("akka", "maven"),
    Defaults.itSettings,

    libraryDependencies ++= ServiceDeps,

    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },

    dockerExposedPorts := Seq(8080, 8558, 2552),
    dockerBaseImage := "openjdk:8-jre-alpine",

    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd("RUN", "/sbin/apk", "add", "--no-cache", "bash", "bind-tools", "busybox-extras", "curl", "strace"),
      Cmd("RUN", "chgrp -R 0 . && chmod -R g=u .")
    ),

    dockerUsername := Some("kubakka"),
    dockerUpdateLatest := true,

    javaOptions in IntegrationTest ++= collection.JavaConverters.propertiesAsScalaMap(System.getProperties)
      .collect { case (key, value) if key.startsWith("akka") => "-D" + key + "=" + value }.toSeq,

  )
  .settings(Defaults.itSettings)
  .enablePlugins(JavaServerAppPackaging)
  .configs(IntegrationTest)

