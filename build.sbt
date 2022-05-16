ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "lakefs"
  )

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-auth" % "3.3.1"

libraryDependencies += "io.lakefs" % "lakefs-spark-client-301_2.12" % "0.1.6"
resolvers += Resolver.mavenLocal