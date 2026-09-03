package domain.parser
import domain.parser.v1.Cron
import domain.parser.v1.{CronLexerAlg, CronValidatorAlg, Monadish}
import domain.parser.given

def parseCron[F[_]: Monadish](
    input: String
)(using
    lexer: CronLexerAlg[F],
    validator: CronValidatorAlg[F]
): F[Cron] = for {
  tok <- lexer.lex(input)
  c <- validator.validate(tok)
} yield c
