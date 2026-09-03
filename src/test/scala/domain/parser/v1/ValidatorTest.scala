package domain.parser.v1

import utils.AbstractCronListFactory

import domain.parser.*

import scala.collection.mutable.ListBuffer

class ValidatorTest extends munit.FunSuite {
  test("it can parse an AbstractCronFragmentList") {
    val validator = new CronValidator
    val acl = AbstractCronListFactory.basic()

    val expectedCron = Cron(
      minute = CronFragment(CronInterval.MINUTE, Wildcard("*")),
      hour = CronFragment(CronInterval.HOUR, Wildcard("*")),
      day_of_month = CronFragment(CronInterval.DAY_OF_MONTH, Wildcard("*")),
      month = CronFragment(CronInterval.MONTH, Wildcard("*")),
      weekday = CronFragment(CronInterval.WEEKDAY, Wildcard("*"))
    )

    validator.validate(acl) match {
      case Right(value) => {
        assertEquals(expectedCron, value)
      }
      case Left(value) => fail(s"Recieved error where none was expected: $value")
    }
  }

  test("it errors when AbstractCronFragmentList length is too small") {
    val validator = new CronValidator
    val acl = AbstractCronListFactory.empty() ++ ListBuffer(Wildcard("*"))

    validator.validate(acl) match {
      case Right(value) => fail("Should have failed with an error")
      case Left(value) => {
        assert(value.isInstanceOf[CronInvalid])
        assertEquals("Provided cron expression * is not a valid cron.", value.message)
      }
    }
  }

  test("it errors when AbstractCronFragmentList length is too big") {
    val validator = new CronValidator
    val acl = AbstractCronListFactory.basic() ++ ListBuffer(Wildcard("*"))

    validator.validate(acl) match {
      case Right(value) => fail("Should have failed with an error")
      case Left(value) => {
        assert(value.isInstanceOf[CronInvalid])
        assertEquals("Provided cron expression * * * * * * is not a valid cron.", value.message)
      }
    }
  }

  test("it checks bounds are semantically correct") {
    val validator = new CronValidator
    val acl = AbstractCronListFactory.empty() ++ ListBuffer(
      Single("99", 99),
      Wildcard("*"),
      Wildcard("*"),
      Wildcard("*"),
      Wildcard("*")
    )

    validator.validate(acl) match {
      case Right(value) => fail("Should have failed with an error")
      case Left(value) => {
        assert(value.isInstanceOf[OutOfIntervalBounds])
        assertEquals("99 outside range of 0 to 59", value.message)
      }
    }
  }
}
