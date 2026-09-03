package domain.parser

import domain.parser.v1.{AbstractCronFragment, CronEither, CronLexerAlg, _}

given CronLexer: CronLexerAlg[CronEither] with {
  override def lex(input: String): CronEither[List[AbstractCronFragment]] =
    val lexer = Lexer.make(input)

    lexer.lex() match {
      case Right(value) => Right(value.toList)
      case Left(value)  => Left(value)
    }
}
