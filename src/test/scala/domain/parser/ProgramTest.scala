package domain.parser

import utils.{TestResult, testInterpreter, given}

class ProgramTest extends munit.FunSuite {
  test("it runs the program successfully") {
    val cron = Cron(
      minute = CronFragment(CronInterval.MINUTE, CronRange("1-3", 1, 3)),
      hour = CronFragment(CronInterval.HOUR, CronRange("1-3", 1, 3)),
      day_of_month = CronFragment(CronInterval.DAY_OF_MONTH, CronRange("1-3", 1, 3)),
      month = CronFragment(CronInterval.MONTH, CronRange("1-3", 1, 3)),
      weekday = CronFragment(CronInterval.WEEKDAY, CronRange("1-3", 1, 3))
    )
    val input = "1-3 1-3 1-3 1-3 1-3"

    val res = program[TestResult](input)(using MonadishTestResult, testInterpreter(Right(cron)))

    assertEquals(input, res.input)
    res.output match {
      case Right(value) =>
        assertEquals(cron, value)
      case Left(value) => fail("Should have suceeded")
    }
  }

  test("it handles non-cron string correctly") {
    val input = "oops"

    val res = program[TestResult](input)(using MonadishTestResult, testInterpreter(Left(CronInvalid(input))))

    assertEquals(input, res.input)
    res.output match {
      case Left(value) =>
        assert(value.isInstanceOf[CronInvalid])
      case Right(value) => fail("Should have failed")
    }
  }

  test("it handles out of bounds item correctly") {
    val input = "99 * * * *"
    val expectedOut = Left(OutOfIntervalBounds(CronInterval.MINUTE, input))

    val res = program[TestResult](input)(using MonadishTestResult, testInterpreter(expected = expectedOut))

    assertEquals(input, res.input)
    res.output match {
      case Left(value) =>
        assert(value.isInstanceOf[OutOfIntervalBounds])
      case Right(value) => fail("Should have failed")
    }
  }
}
