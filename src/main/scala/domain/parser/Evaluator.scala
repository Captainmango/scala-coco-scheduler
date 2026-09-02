package domain.parser

object CronEvaluator {
    def evaluate(c: Cron): String = {
        val Cron(mi, h, dom, m, w) = c
        val out = List(mi, h, dom, m, w).map(cf => {
            s"%-15s | %s".format(getIntervalString(cf.interval), getPossibleValues(cf).mkString(","))
        })
        out.mkString("\n")
    }

    private def getIntervalString(ci: CronInterval): String = 
        ci match {
            case CronInterval.MINUTE => "minute"
            case CronInterval.HOUR => "hour"
            case CronInterval.DAY_OF_MONTH => "day of month"
            case CronInterval.MONTH => "month"
            case CronInterval.WEEKDAY => "weekday"
        }
    
    private def getPossibleValues(cf: CronFragment): List[Int] = {
        val bottom = cf.interval.lowerBound
        val top = cf.interval.upperBound

        cf.abstractFragment match
            case Single(raw, value) => List(value)
            case Range(raw, bottom, top) => (bottom to top).toList
            case CronList(raw, items) => items
            case Divisor(raw, operand) => (bottom to top).filter(n => Math.floorMod(n, operand) == 0).toList
            case Wildcard(raw) => (bottom to top).toList
    }
}
