package domain.parser

import domain.parser.CronRange

class EvaluatorTest extends munit.FunSuite {
    test("it can parse a cron") {
        val cron = Cron (
            minute = CronFragment(CronInterval.MINUTE, CronRange("1-3", 1, 3)),
            hour = CronFragment(CronInterval.HOUR, CronRange("1-3", 1, 3)),
            day_of_month = CronFragment(CronInterval.DAY_OF_MONTH, CronRange("1-3", 1, 3)),
            month = CronFragment(CronInterval.MONTH, CronRange("1-3", 1, 3)),
            weekday = CronFragment(CronInterval.WEEKDAY, CronRange("1-3", 1, 3)),
        )

        val valueFormat = (ci: String, vals: String) => s"%-15s | %s".format(ci, vals)

        val res = CronEvaluator.evaluate(cron)
        assert(res.contains(valueFormat("minute", "1,2,3")))
        assert(res.contains(valueFormat("hour", "1,2,3")))
        assert(res.contains(valueFormat("day of month", "1,2,3")))
        assert(res.contains(valueFormat("month", "1,2,3")))
        assert(res.contains(valueFormat("weekday", "1,2,3")))
    }
}
