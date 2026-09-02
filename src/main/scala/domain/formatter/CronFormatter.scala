package domain.formatter

import domain.parser.Cron
import domain.parser.RawCron
import domain.parser.CronEvaluator

object CronFormatter {
    def presentRaw(c: Cron): String = {
        val RawCron(mi, h, dom, m, w) = c
        f"$mi $h $dom $m $w"
    }

    def presentPossibleValues(c: Cron): String = 
        CronEvaluator.evaluate(c)
}
