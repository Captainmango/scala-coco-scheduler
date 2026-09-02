package domain.parser

import utils.AbstractCronListFactory
import scala.collection.mutable.ListBuffer

class ParserTest extends munit.FunSuite {
    test("it can parse an AbstractCronFragmentList") {
        val parser = new CronParser
        val acl = AbstractCronListFactory.basic()

        val expectedCron = Cron(
            minute = CronFragment(CronInterval.MINUTE, Wildcard("*")),
            hour = CronFragment(CronInterval.HOUR, Wildcard("*")),
            day_of_month = CronFragment(CronInterval.DAY_OF_MONTH, Wildcard("*")),
            month = CronFragment(CronInterval.MONTH, Wildcard("*")),
            weekday = CronFragment(CronInterval.WEEKDAY, Wildcard("*")),
        )

        parser.parse(acl) match {
            case Right(value) => {
                assertEquals(expectedCron, value)
            }
            case Left(value) => fail(s"Recieved error where none was expected: $value")
        }
    }

    test("it errors when AbstractCronFragmentList length is too small") {
        val parser = new CronParser
        val acl = AbstractCronListFactory.empty() ++ ListBuffer(Wildcard("*"))

        parser.parse(acl) match {
            case Right(value) => fail("Should have failed with an error")
            case Left(value) => {
                assert(value.isInstanceOf[CronInvalid])
                assertEquals("Provided cron expression * is not a valid cron.", value.toString())
            }
        }
    }

    test("it errors when AbstractCronFragmentList length is too big") {
        val parser = new CronParser
        val acl = AbstractCronListFactory.basic() ++ ListBuffer(Wildcard("*"))

        parser.parse(acl) match {
            case Right(value) => fail("Should have failed with an error")
            case Left(value) => {
                assert(value.isInstanceOf[CronInvalid])
                assertEquals("Provided cron expression * * * * * * is not a valid cron.", value.toString())
            }
        }
    }


    test("it checks bounds are semantically correct") {
        val parser = new CronParser
        val acl = AbstractCronListFactory.empty() ++ ListBuffer(
            Single("99", 99),
            Wildcard("*"),
            Wildcard("*"),
            Wildcard("*"),
            Wildcard("*"),
        )

        parser.parse(acl) match {
            case Right(value) => fail("Should have failed with an error")
            case Left(value) => {
                assert(value.isInstanceOf[OutOfIntervalBounds])
                assertEquals("99 outside range of 0 to 59", value.toString())
            }
        }
    }
}
