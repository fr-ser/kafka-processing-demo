name := "reading_filter"

version := "0.1"

scalaVersion := "2.13.4"

// https://stackoverflow.com/questions/41372978/unknown-artifact-not-resolved-or-indexed-error-for-scalatest
ThisBuild / useCoursier := false
// https://github.com/sbt/sbt/issues/5263#issuecomment-626462593
updateSbtClassifiers / useCoursier := true

// kafka
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.6.0"
libraryDependencies += "org.apache.kafka" % "kafka-streams-test-utils" % "2.6.0" % Test
// logging
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// circe JSON serde
libraryDependencies += "io.circe" %% "circe-core" % "0.13.0"
libraryDependencies += "io.circe" %% "circe-parser" % "0.13.0"
libraryDependencies += "io.circe" %% "circe-jawn" % "0.13.0"
libraryDependencies += "io.circe" %% "circe-derivation" % "0.13.0-M5"
libraryDependencies += "io.circe" %% "circe-generic" % "0.13.0" % Test
// testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % Test
