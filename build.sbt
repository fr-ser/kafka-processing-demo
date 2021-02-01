
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.0.1"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"
ThisBuild / name := "reading_filter"

Global / lintUnusedKeysOnLoad := false

// https://stackoverflow.com/questions/41372978/unknown-artifact-not-resolved-or-indexed-error-for-scalatest
ThisBuild / useCoursier := false
// https://github.com/sbt/sbt/issues/5263#issuecomment-626462593
updateSbtClassifiers / useCoursier := true

enablePlugins(CucumberPlugin)

CucumberPlugin.monochrome := false
CucumberPlugin.glues := List("feature")
CucumberPlugin.mainClass := "io.cucumber.core.cli.Main"

lazy val root = (project in file("."))
  .settings(
    // kafka
    libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.6.0",
    libraryDependencies += "org.apache.kafka" % "kafka-streams-test-utils" % "2.6.0" % Test,
    // config loading
    libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.14.0",
    // logging
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    // circe JSON serde
    libraryDependencies += "io.circe" %% "circe-core" % "0.13.0",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.13.0",
    libraryDependencies += "io.circe" %% "circe-jawn" % "0.13.0",
    libraryDependencies += "io.circe" %% "circe-derivation" % "0.13.0-M5",
    // testing
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % Test,
    libraryDependencies += "io.cucumber" %% "cucumber-scala" % "4.7.1" % Test,
    libraryDependencies +=  "io.cucumber" % "cucumber-jvm" % "4.7.1" % Test,
    libraryDependencies +=  "io.cucumber" % "cucumber-junit" % "4.7.1" % Test,


    // for sbt assembly
    mainClass in assembly := Some("com.example.reading_filter.App"),
    assemblyOutputPath in assembly := file("target/reading_filter.jar"),
    assemblyMergeStrategy in assembly := {
      case s if s.endsWith("module-info.class") => MergeStrategy.discard
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
  )
