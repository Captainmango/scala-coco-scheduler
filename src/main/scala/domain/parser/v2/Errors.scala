package domain.parser.v2

sealed trait ParserError { def message: String }
case class InvalidInput() extends ParserError {
  def message: String = "Given cron expression is invalid"
}
case class ExpectedNumber(raw: String, position: Int) extends ParserError {
  def message: String = s"Expected number at position $position. Near $raw"
}
case class ExpressionInvalid(rawExpr: String) extends ParserError {
  def message: String = s"Provided cron expression $rawExpr is not valid."
}
case class OutOfIntervalBounds(ci: CronInterval) extends ParserError {
  def message: String = s"outside range of ${ci.lowerBound} to ${ci.upperBound}"
}
case class UnprocessableNumber(v: String) extends ParserError {
  def message: String = s"$v is not a valid number"
}
case class IncorrectExpressionLength(v: Int) extends ParserError {
  def message: String = s"Length of expression is incorrect. Expected 5. Got length $v"
}
