name := "spark-examples"

version := "1.0.0"

scalacOptions ++= Seq("-deprecation")

scalaVersion := "2.10.4"

libraryDependencies ++= Seq (
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.2",
  ("org.apache.spark" %% "spark-core" % "2.0.2" % "provided"),
  ("org.apache.spark" %% "spark-sql" % "2.0.2" % "provided")
)
