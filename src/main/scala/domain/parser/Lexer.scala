package domain.parser

import domain.parser.v2._

given CronLexer: CronLexerAlg[CronEither] with {
  override def lex(input: String): CronEither[List[List[CronLexem]]] =
    CronLexerV2.lex(input)
}
