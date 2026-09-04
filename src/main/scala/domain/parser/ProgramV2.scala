package domain.parser
import domain.parser.v2._

def parseCron[F[_]: Monadish](
    input: String
)(using
    lexer: CronLexerAlg[F],
    parser: CronParserAlg[F],
    evaluator: CronEvaluatorAlg[F]
): F[Cron] = for {
  lexems <- lexer.lex(input)
  operations <- parser.parse(lexems)
  cron <- evaluator.evaluate(operations)
} yield cron
