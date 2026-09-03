package domain.parser

import scala.collection.mutable.ListBuffer
import domain.parser.v1.*



given CronValidatorIntp: CronValidatorAlg[CronEither] with {
  override def validate(input: List[AbstractCronFragment]): CronEither[Cron] =
    new CronValidator().validate(ListBuffer.from(input))

}
