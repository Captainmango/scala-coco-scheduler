package domain.parser

import scala.collection.mutable.ListBuffer
import scala.util.boundary
import scala.util.boundary.break

class CronParser {
  private val cronIntervalOrder = List(
    CronInterval.MINUTE,
    CronInterval.HOUR,
    CronInterval.DAY_OF_MONTH,
    CronInterval.MONTH,
    CronInterval.WEEKDAY
  )

  def parse(input: AbstractCronFragmentList): Either[ParserError, Cron] = {
    if (input.length != cronIntervalOrder.length) then return Left(CronInvalid(input.toRaw()))

    val items: Either[ParserError, ListBuffer[CronFragment]] = boundary {
      val frags = input
        .zip(cronIntervalOrder)
        .map((acf, ci) => {
          val boundsCheck: (p: Int) => Boolean = (p => (ci.lowerBound to ci.upperBound).contains(p))

          acf match {
            case CronList(_, items) if (items.forall(boundsCheck))                    => CronFragment(ci, acf)
            case Divisor(_, operand) if boundsCheck(operand)                          => CronFragment(ci, acf)
            case CronRange(_, bottom, top) if boundsCheck(bottom) && boundsCheck(top) => CronFragment(ci, acf)
            case Single(_, value) if boundsCheck(value)                               => CronFragment(ci, acf)
            case Wildcard(_)                                                          => CronFragment(ci, acf)
            case _ => break(Left(OutOfIntervalBounds(ci, acf.raw)))
          }
        })
      Right(frags)
    }

    items match {
      case Right(value) => Right(Cron.fromCronFragments(value))
      case Left(value)  => Left(value)
    }
  }
}
