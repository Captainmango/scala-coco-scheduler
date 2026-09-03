package domain.parser

import domain.parser.v1._
import utils.given
import utils.{TestResult, Traces, testLexer, testValidator}

class ParserTest extends munit.FunSuite {
  test("it runs the program successfully") {

    val lexResult = Right(
      List(
        CronRange("1-3", 1, 3),
        CronRange("1-3", 1, 3),
        CronRange("1-3", 1, 3),
        CronRange("1-3", 1, 3),
        CronRange("1-3", 1, 3)
      )
    )

    val validateResult = Right(
      Cron(
        minute = CronFragment(CronInterval.MINUTE, CronRange("1-3", 1, 3)),
        hour = CronFragment(CronInterval.HOUR, CronRange("1-3", 1, 3)),
        day_of_month = CronFragment(CronInterval.DAY_OF_MONTH, CronRange("1-3", 1, 3)),
        month = CronFragment(CronInterval.MONTH, CronRange("1-3", 1, 3)),
        weekday = CronFragment(CronInterval.WEEKDAY, CronRange("1-3", 1, 3))
      )
    )

    given CronLexerAlg[TestResult] = testLexer(lexResult)
    given CronValidatorAlg[TestResult] = testValidator(validateResult)

    val input = "1-3 1-3 1-3 1-3 1-3"

    val res = parseCron[TestResult](input)(using MonadishTestResult)

    res.traces match {
      case List(Traces.Lexed(actualIn), Traces.Validated(tokens)) => {
        assertEquals(input, actualIn)
        assertEquals(lexResult.right.get, tokens)
      }
      case _ => fail("traces not correct")
    }
  }

  test("it handles non-cron string correctly") {
    val input = "oops"

    given CronLexerAlg[TestResult] = testLexer(Left(CronInvalid(input)))
    given CronValidatorAlg[TestResult] = testValidator(Left(CronInvalid("Should not be reached")))

    val res = parseCron[TestResult](input)(using MonadishTestResult)

    res.traces match {
      case List(Traces.Lexed(actualIn)) => {
        assertEquals(input, actualIn)
      }
      case _ => fail("traces not correct")
    }

    res.output match {
      case Left(value) => {
        assert(value.isInstanceOf[CronInvalid])
      }
      case Right(value) => fail("Should have failed with message")
    }
  }

  test("it handles out of bounds item correctly") {
    val input = "99 * * * *"

    given CronLexerAlg[TestResult] = testLexer(Left(OutOfIntervalBounds(CronInterval.MINUTE, input)))
    given CronValidatorAlg[TestResult] = testValidator(Left(CronInvalid("Should not be reached")))

    val res = parseCron[TestResult](input)(using MonadishTestResult)

    res.traces match {
      case List(Traces.Lexed(actualIn)) => {
        assertEquals(input, actualIn)
      }
      case _ => fail("traces not correct")
    }

    res.output match {
      case Left(value) => {
        assert(value.isInstanceOf[OutOfIntervalBounds])
      }
      case Right(value) => fail("Should have failed with message")
    }
  }
}
