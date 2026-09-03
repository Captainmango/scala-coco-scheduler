package domain.parser

def program[F[_]: Monadish](input: String)(using parser: CronParserAlg[F]): F[Cron] = for {
  c <- parser.parse(input)
} yield c
