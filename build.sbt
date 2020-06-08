import com.typesafe.sbt.packager.docker.DockerChmodType

name := "association-service"

version := "1.0"

scalaVersion := "2.13.0"

lazy val akkaVersion = "2.6.5"
lazy val akkaGrpcVersion = "0.8.4"

enablePlugins(AkkaGrpcPlugin)
enablePlugins(JavaAppPackaging)

// ALPN agent
enablePlugins(JavaAgent)
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test"
bashScriptExtraDefines ++= Seq( "addJava '-Dpidfile.path=/dev/null'" )
dockerAdditionalPermissions += (DockerChmodType.UserGroupWriteExecute, "/opt/docker")
dockerExposedPorts ++= Seq(9000, 9001)

libraryDependencies ++= Seq(
  "org.neo4j.driver" % "neo4j-java-driver" % "1.7.5",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.dimafeng" %% "neotypes" % "0.13.2",
  "com.dimafeng" %% "neotypes-akka-stream" % "0.13.2",
  "com.google.inject" % "guice" % "4.2.3",
  "com.google.api.grpc" % "grpc-google-common-protos"           % "1.16.0" % "protobuf",
  "org.typelevel"       %% "cats-core"                          % "2.0.0",
  "org.scala-lang.modules" %% "scala-async" % "0.10.0"
)
