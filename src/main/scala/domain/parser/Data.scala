package domain.parser


enum CronInterval(val lowerBound: Int, val upperBound: Int) {
    case MINUTE extends CronInterval(0, 59)
    case HOUR extends CronInterval(0, 23)
    case DAY_OF_MONTH extends CronInterval(1, 31)
    case MONTH extends CronInterval(1, 12)
    case WEEKDAY extends CronInterval(1, 7)
}

sealed trait AbstractCronFragment
case class Single(val raw: String, val value: Int) extends AbstractCronFragment
case class Range(val raw: String, val bottom: Int, val top: Int) extends AbstractCronFragment
case class CronList(val raw: String, val items: List[Int]) extends AbstractCronFragment
case class Divisor(val raw: String, val operand: Int) extends AbstractCronFragment
case class Wildcard(val raw: String) extends AbstractCronFragment

type AbstractCronFragmentList = List[AbstractCronFragment]

case class CronFragment(val interval: CronInterval, val abstractFragment: AbstractCronFragment)

case class Cron(
    val minute: CronFragment,
    val hour: CronFragment,
    val day_of_month: CronFragment,
    val month: CronFragment,
    val weekday: CronFragment
)