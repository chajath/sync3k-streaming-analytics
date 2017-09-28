name := "sync3k-streaming-analytics"

version := "0.1"

scalaVersion := "2.11.11"

fork := true

outputStrategy := Some(StdoutOutput)

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

scalacOptions += "-target:jvm-1.8"
