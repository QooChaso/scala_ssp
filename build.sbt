name := "Intern_SSP"
 
version := "1.0"

import play.sbt.PlayScala
lazy val `intern_ssp` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice, ws, ehcache )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

      