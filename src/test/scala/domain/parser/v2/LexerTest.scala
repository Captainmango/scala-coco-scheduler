package domain.parser.v2

class LexerTest extends munit.FunSuite {
  test("it handles a single asterisk") {
    val res = CronLexer.lex(("*"))

    res match {
      case Right(value) =>
        assertEquals(List(Asterisk), value)
      case Left(value) => fail("should have succeeded")
    }
  }

  test("it handles the full grammar") {
    val res = CronLexer.lex(("*/,-99"))

    res match {
      case Right(value) =>
        assertEquals(List(Asterisk, Slash, Comma, Dash, Number("99")), value)
      case Left(value) => fail("should have succeeded")
    }
  }

  test("it errors on non-grammar things") {
    val res = CronLexer.lex("a")

    res match
      case Left(value) =>
        assert(value.isInstanceOf[InvalidInput])
        assertEquals("Invalid input at position 0. Near a", value.message)
      case Right(value) => fail("Should not have succeeded")
  }

  test("it errors on bad number") {
    val res = CronLexer.lex("62a")

    res match
      case Left(value) =>
        assert(value.isInstanceOf[InvalidInput])
        assertEquals("Invalid input at position 2. Near 62a", value.message)
      case Right(value) => fail("Should not have succeeded")
  }
}
