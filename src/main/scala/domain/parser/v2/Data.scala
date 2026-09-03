package domain.parser.v2

enum CronInterval(val lowerBound: Int, val upperBound: Int) {
  case MINUTE extends CronInterval(0, 59)
  case HOUR extends CronInterval(0, 23)
  case DAY_OF_MONTH extends CronInterval(1, 31)
  case MONTH extends CronInterval(1, 12)
  case WEEKDAY extends CronInterval(1, 7)
}

sealed trait CronLexem {
    def raw: String
}
case class Wildcard() extends CronLexem {
    override def raw: String = "*"
}
case class Divisor(operand: Int) extends CronLexem {
    override def raw: String = s"*/$operand"
}
case class CRange(bottom: Int, top: Int) extends CronLexem {
    override def raw: String = s"$bottom-$top"
}
case class CList(nums: List[Int]) extends CronLexem {
    override def raw: String = nums.mkString(",")
}
case class Single(num: Int) extends CronLexem {
    override def raw: String = s"$num"
}

case class CronFragment(ci: CronInterval, cl: CronLexem)

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
            case _ => None
        }
    }
}