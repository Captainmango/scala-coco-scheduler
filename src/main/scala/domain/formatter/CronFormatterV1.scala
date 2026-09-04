package domain.formatter

import domain.parser.v1.{Cron, CronEvaluator, RawCron}
import domain.parser.v2.{Cron => Cronv2}
import domain.parser.v2.CronInterval
import domain.parser.v2.CronFragment
import domain.parser.v2.Wildcard
import domain.parser.v2.CList
import domain.parser.v2.CRange
import domain.parser.v2.Divisor
import domain.parser.v2.Single

object CronFormatterV1 {
  def presentRaw(c: Cron): String = {
    val RawCron(mi, h, dom, m, w) = c
    f"$mi $h $dom $m $w"
  }

  def presentPossibleValues(c: Cron): String =
    CronEvaluator.evaluate(c)
}

object CronFormatterV2 {
  def presentPossibleValues(c: Cronv2): String = {
    val Cronv2(mi, h, dom, m, w) = c
    val out = List(mi, h, dom, m, w).map(cf => {
      "%-15s | %s".format(getIntervalString(cf.ci), getPossibleValues(cf).mkString(","))
    })
    out.mkString("\n")
  }

  private def getIntervalString(ci: CronInterval): String =
    ci match {
      case CronInterval.MINUTE       => "minute"
      case CronInterval.HOUR         => "hour"
      case CronInterval.DAY_OF_MONTH => "day of month"
      case CronInterval.MONTH        => "month"
      case CronInterval.WEEKDAY      => "weekday"
    }

  private def getPossibleValues(cf: CronFragment): List[Int] = {
    val bottom = cf.ci.lowerBound
    val top = cf.ci.upperBound

    cf.co match {
      case Wildcard()             => (bottom to top).toList
      case CList(items)           => items
      case CRange(brange, trange) => (brange to trange).toList
      case Divisor(operand)       => (bottom to top).filter(n => Math.floorMod(n, operand) == 0).toList
      case Single(num)            => List(num)
    }
  }
}
