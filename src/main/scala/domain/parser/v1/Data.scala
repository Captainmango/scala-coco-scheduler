package domain.parser.v1

import scala.collection.mutable.ListBuffer

enum CronInterval(val lowerBound: Int, val upperBound: Int) {
  case MINUTE extends CronInterval(0, 59)
  case HOUR extends CronInterval(0, 23)
  case DAY_OF_MONTH extends CronInterval(1, 31)
  case MONTH extends CronInterval(1, 12)
  case WEEKDAY extends CronInterval(1, 7)
}

sealed trait AbstractCronFragment {
  def raw: String
}
case class Single(val raw: String, val value: Int) extends AbstractCronFragment
case class CronRange(val raw: String, val bottom: Int, val top: Int) extends AbstractCronFragment
case class CronList(val raw: String, val items: List[Int]) extends AbstractCronFragment
case class Divisor(val raw: String, val operand: Int) extends AbstractCronFragment
case class Wildcard(val raw: String) extends AbstractCronFragment

type AbstractCronFragmentList = ListBuffer[AbstractCronFragment]

extension (acl: AbstractCronFragmentList) {
  def toRaw(): String = acl.map(acf => acf.raw).mkString(" ")
}

case class CronFragment(val interval: CronInterval, val abstractFragment: AbstractCronFragment) {
  override def toString(): String = abstractFragment.raw
}

case class Cron(
    val minute: CronFragment,
    val hour: CronFragment,
    val day_of_month: CronFragment,
    val month: CronFragment,
    val weekday: CronFragment
)

object Cron {
  def fromCronFragments(l: ListBuffer[CronFragment]): Cron =
    l match {
      case ListBuffer(mi, h, dom, m, w) =>
        Cron(mi, h, dom, m, w)
      case _: ListBuffer[?] => throw new RuntimeException
    }
}

object RawCron {
  def unapply(c: Cron): (String, String, String, String, String) =
    (
      c.minute.abstractFragment.raw,
      c.hour.abstractFragment.raw,
      c.day_of_month.abstractFragment.raw,
      c.month.abstractFragment.raw,
      c.weekday.abstractFragment.raw
    )
}
