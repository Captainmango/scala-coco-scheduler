package domain.parser

import domain.parser.v2._

given CronParser: CronParserAlg[CronEither] with {
  override def parse(input: List[List[CronLexem]]): CronEither[List[CronOperation]] =
    CronParserV2.parse(input)
}
