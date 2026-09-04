package domain.parser.v2

class ParserTest extends munit.FunSuite {
  test("it can handle correct wildcard") {
    val lexed = List(List(Asterisk))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => {
        assertEquals(List(Wildcard()), value)
      }
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it can handle correct divisor") {
    val lexed = List(List(Asterisk, Slash, Number("15")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => {
        assertEquals(List(Divisor(15)), value)
      }
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it can handle correct range") {
    val lexed = List(List(Number("1"), Dash, Number("15")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => {
        assertEquals(List(CRange(1, 15)), value)
      }
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it can handle correct list") {
    val lexed = List(List(Number("1"), Comma, Number("15")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => {
        assertEquals(List(CList(List(1, 15))), value)
      }
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it can handle correct single") {
    val lexed = List(List(Number("1")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => {
        assertEquals(List(Single(1)), value)
      }
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it can handles unmatched pattern") {
    val lexed = List(List(Number("1"), Dash))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) => fail("Should not have succeeded")
      case Left(value) => {
        assert(value.isInstanceOf[ExpressionInvalid])
        assertEquals("Provided cron expression 1- is not valid.", value.message)
      }
    }
  }

  test("it corrects backwards list") {
    val lexed = List(List(Number("7"), Comma, Number("1")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) =>
        assertEquals(List(CList(List(1, 7))), value)
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }

  test("it corrects backwards range") {
    val lexed = List(List(Number("7"), Dash, Number("1")))

    val res = CronParserV2.parse(lexed)

    res match {
      case Right(value) =>
        assertEquals(List(CRange(1, 7)), value)
      case Left(value) => fail(s"Got unexpected error: ${value.message}")
    }
  }
}
