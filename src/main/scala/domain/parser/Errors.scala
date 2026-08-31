package domain.parser

sealed trait ParserError {
    def message(): String
}
case class InvalidInput(raw: String, position: Int) extends ParserError {
    override def message(): String = s"Invalid input at $position. Near $raw"
}
case class ExpectedNumber(raw: String, position: Int) extends ParserError {
    override def message(): String = s"Expected Number at $position. Near $raw"
}