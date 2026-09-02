package domain.formatter

import domain.parser.{Cron, CronEvaluator, RawCron}

object CronFormatter {
  def presentRaw(c: Cron): String = {
    val RawCron(mi, h, dom, m, w) = c
    f"$mi $h $dom $m $w"
  }

  def presentPossibleValues(c: Cron): String =
    CronEvaluator.evaluate(c)
}
