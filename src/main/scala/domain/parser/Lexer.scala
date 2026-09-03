package domain.parser

import domain.parser.v1._
import domain.parser.v1.AbstractCronFragment
import domain.parser.v1.{CronEither, CronLexerAlg}

given CronLexer: CronLexerAlg[CronEither] with {
  override def lex(input: String): CronEither[List[AbstractCronFragment]] =
    val lexer = Lexer.make(input)

    lexer.lex() match {
      case Right(value) => Right(value.toList)
      case Left(value)  => Left(value)
    }
}
