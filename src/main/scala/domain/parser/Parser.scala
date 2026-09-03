package domain.parser

def parseCron[F[_]: Monadish](
    input: String
)(using
    lexer: CronLexerAlg[F],
    validator: CronValidatorAlg[F]
): F[Cron] = for {
  tok <- lexer.lex(input)
  c <- validator.validate(tok)
} yield c
