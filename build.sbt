name := "sandwhich"

version := "0.9"

scalaVersion := 2.10.2

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.2-M3",
  "com.typesafe.akka" %% "akka-slf4j"   % "2.2-M3",
  "com.typesafe.akka" %% "akka-remote"  % "2.2-M3",
  "com.typesafe.akka" %% "akka-agent"   % "2.2-M3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2-M3" % "test"
)
