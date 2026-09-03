package domain.parser

import domain.parser.v1._

import scala.collection.mutable.ListBuffer

given CronValidatorIntp: CronValidatorAlg[CronEither] with {
  override def validate(input: List[AbstractCronFragment]): CronEither[Cron] =
    new CronValidator().validate(ListBuffer.from(input))

}
