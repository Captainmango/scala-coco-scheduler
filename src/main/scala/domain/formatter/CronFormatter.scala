package domain.formatter

import domain.parser.v1.{Cron, CronEvaluator, RawCron}

object CronFormatter {
  def presentRaw(c: Cron): String = {
    val RawCron(mi, h, dom, m, w) = c
    f"$mi $h $dom $m $w"
  }

  def presentPossibleValues(c: Cron): String =
    CronEvaluator.evaluate(c)
}
