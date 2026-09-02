package domain.parser

sealed trait ParserError
case class InvalidInput(raw: String, position: Int) extends ParserError {
  override def toString(): String = s"Invalid input at position $position. Near $raw"
}
case class ExpectedNumber(raw: String, position: Int) extends ParserError {
  override def toString(): String = s"Expected number at position $position. Near $raw"
}
case class CronInvalid(rawExpr: String) extends ParserError {
  override def toString(): String = s"Provided cron expression $rawExpr is not a valid cron."
}
case class OutOfIntervalBounds(ci: CronInterval, v: String) extends ParserError {
  override def toString(): String = s"$v outside range of ${ci.lowerBound} to ${ci.upperBound}"
}
