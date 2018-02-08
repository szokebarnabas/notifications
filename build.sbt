import sbt.Keys._

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")


lazy val root = (project in file(".")).
  settings(
    name := "notifications",
    version := "0.1",
    organization := "com.bs",
    scalaVersion := "2.12.1",
    mainClass in Compile := Some("com.bs.notifications.SqsToEmailStreamer")
  )

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += Resolver.bintrayRepo("lightshed", "maven")

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "0.16",
  "org.json4s" % "json4s-jackson_2.12" % "3.5.0",
  "org.json4s" % "json4s-ext_2.12" % "3.5.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.akka" % "akka-slf4j_2.12" % "2.4.17",
  "ch.lightshed" %% "courier" % "0.1.4"
)

assemblyJarName in assembly := s"${name.value}.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}