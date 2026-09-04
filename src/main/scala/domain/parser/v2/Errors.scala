package domain.parser.v2

sealed trait ParserError { def message: String }
case class InvalidInput(raw: String, position: Int) extends ParserError {
  def message: String = s"Invalid input at position $position. Near $raw"
}
case class ExpectedNumber(raw: String, position: Int) extends ParserError {
  def message: String = s"Expected number at position $position. Near $raw"
}
case class ExpressionInvalid(rawExpr: String) extends ParserError {
  def message: String = s"Provided cron expression $rawExpr is not valid."
}
case class OutOfIntervalBounds(ci: CronInterval, v: String) extends ParserError {
  def message: String = s"$v outside range of ${ci.lowerBound} to ${ci.upperBound}"
}
case class UnprocessableNumber(v: String) extends ParserError {
  def message: String = s"$v is not a valid number"
}
