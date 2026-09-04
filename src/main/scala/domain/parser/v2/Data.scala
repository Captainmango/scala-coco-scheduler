package domain.parser.v2

enum CronInterval(val lowerBound: Int, val upperBound: Int) {
  case MINUTE extends CronInterval(0, 59)
  case HOUR extends CronInterval(0, 23)
  case DAY_OF_MONTH extends CronInterval(1, 31)
  case MONTH extends CronInterval(1, 12)
  case WEEKDAY extends CronInterval(1, 7)
}

sealed trait CronLexem { def raw: String }
object Asterisk extends CronLexem { override def raw: String = "*" }
object Slash extends CronLexem { override def raw: String = "/" }
object Dash extends CronLexem { override def raw: String = "-" }
object Comma extends CronLexem { override def raw: String = "," }
case class Number(raw: String) extends CronLexem

sealed trait CronOperation
case class Wildcard() extends CronOperation
case class Divisor(operand: Int) extends CronOperation
case class CRange(bottom: Int, top: Int) extends CronOperation
case class CList(items: List[Int]) extends CronOperation
case class Single(num: Int) extends CronOperation

case class CronFragment(ci: CronInterval, co: CronOperation)

case class Cron(
    minute: CronFragment,
    hour: CronFragment,
    day_of_month: CronFragment,
    month: CronFragment,
    weekday: CronFragment
)

object Cron {
  def apply(l: List[CronFragment]): Option[Cron] = {
    l match {
      case List(mi, h, dom, m, w) => Some(Cron(mi, h, dom, m, w))
      case _                      => None
    }
  }
}
