package domain.parser

import domain.parser.v2._

given CronEvaluator: CronEvaluatorAlg[CronEither] with {
  override def evaluate(input: List[CronOperation]): CronEither[Cron] = ???
}
