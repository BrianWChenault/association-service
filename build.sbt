name := "association-service"

version := "1.0"

scalaVersion := "2.13.2"

lazy val akkaVersion = "2.6.5"
lazy val akkaGrpcVersion = "0.8.4"

enablePlugins(AkkaGrpcPlugin)

// ALPN agent
enablePlugins(JavaAgent)
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.michaelpollmeier"   %% "gremlin-scala" % "3.4.4.5",
  "org.apache.tinkerpop" % "gremlin-driver" % "3.3.1",
  "com.steelbridgelabs.oss" % "neo4j-gremlin-bolt" % "0.4.1",
  "org.neo4j"              % "neo4j-tinkerpop-api-impl" % "0.7-3.2.3",
  "com.google.inject" % "guice" % "4.2.3",
  "com.google.api.grpc" % "grpc-google-common-protos"           % "1.16.0" % "protobuf"
)
