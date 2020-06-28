lazy val root = (project in file(".")).
settings (
  name := "TicketKiosk",
  version := "1.0",
  scalaVersion := "2.12.8",
  scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature"),
  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.5.22")
)
