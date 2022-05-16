trait Loggable {
  lazy val log = org.apache.log4j.LogManager.getLogger(this.getClass)
}
