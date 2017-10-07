name := "sync3k-streaming-analytics"

version := "0.1"

scalaVersion := "2.11.11"

fork := true

outputStrategy := Some(StdoutOutput)

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.3"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.11.0.1"

libraryDependencies += "com.github.scopt"  %% "scopt" % "3.6.0"

scalacOptions += "-target:jvm-1.8"

assemblyJarName in assembly := name.value + ".jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
