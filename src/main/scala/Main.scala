import domain.parser.Parser
import domain.parser.Lexer

@main
def hello(): Unit =
  val parser = new Parser(Lexer.make(""))
  val result = parser.parse("99 * * *") match {
    case Left(value) => s"This errored: ${value}"
    case Right(value) => value
  }

  println(result)
