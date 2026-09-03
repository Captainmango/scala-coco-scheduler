package domain.parser

import scala.collection.mutable.ListBuffer

class LexerTest extends munit.FunSuite {
  test("it can reset the input") {
    val l = Lexer.make("test-input")
    val currentInput = l.input()

    val expectedInput = "new-input"

    l.input = expectedInput
    assertNotEquals(currentInput, l.input())
    assertEquals(l.input(), expectedInput)
  }

  test("it can lex single operator") {
    val l = Lexer.make("*")
    val expectedOut = ListBuffer[AbstractCronFragment](Wildcard("*"))
    val res = l.lex() match {
      case Left(e)    => fail(s"Recieved unexpected error $e}")
      case Right(out) => out
    }

    assertEquals(expectedOut, res)
  }

  test("it can lex two operators") {
    val l = Lexer.make("* */15")
    val expectedOut = ListBuffer[AbstractCronFragment](Wildcard("*"), Divisor("*/15", 15))

    val res = l.lex().right.get
    assertEquals(expectedOut, res)
  }

  test("it errors if input isn't correct") {
    val l = Lexer.make("*a")

    l.lex() match {
      case Left(value) => {
        assert(value.isInstanceOf[InvalidInput])
        assertEquals("Invalid input at position 1. Near *a", value.message)
      }
      case Right(value) => fail("Should have failed with error")
    }
  }

  test("it errors if it cannot read number where a number should be") {
    val l = Lexer.make("*/ddd")

    l.lex() match {
      case Left(value) => {
        assert(value.isInstanceOf[ExpectedNumber])
        assertEquals("Expected number at position 2. Near */d", value.message)
      }
      case Right(value) => fail("Should have failed with error")
    }
  }

  test("it errors if not valid input") {
    val l = Lexer.make("d")

    l.lex() match {
      case Left(value) => {
        assert(value.isInstanceOf[InvalidInput])
        assertEquals("Invalid input at position 0. Near d", value.message)
      }
      case Right(value) => fail("Should have failed with error")
    }
  }

  test("it handles single") {
    val l = Lexer.make("2")
    val expectedOut: AbstractCronFragmentList = ListBuffer(Single("2", 2))
    l.lex() match {
      case Left(e) => fail(s"Received unexpected error $e")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }

  test("it handles multiple digits") {
    val l = Lexer.make("2101")
    val expectedOut: AbstractCronFragmentList = ListBuffer(Single("2101", 2101))
    l.lex() match {
      case Left(e) => fail(s"Received unexpected error ${e.message}")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }

  test("it handles range") {
    val l = Lexer.make("1-5")
    val expectedOut: AbstractCronFragmentList = ListBuffer(CronRange("1-5", bottom = 1, top = 5))
    l.lex() match {
      case Left(e) => fail(s"Received unexpected error ${e.message}")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }

  test("it handles basic list") {
    val l = Lexer.make("1,5")
    val expectedOut: AbstractCronFragmentList = ListBuffer(CronList("1,5", List(1, 5)))
    l.lex() match {
      case Left(e) => fail(s"Received unexpected error ${e.message}")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }

  test("it handles malformed list") {
    val l = Lexer.make("1,a")
    l.lex() match {
      case Left(e) => {
        assert(e.isInstanceOf[ExpectedNumber])
        assertEquals("Expected number at position 2. Near 1,a", e.message)
      }
      case Right(value) => fail("Expected to fail with error")
    }
  }

  test("it handles malformed range") {
    val l = Lexer.make("1-a")
    l.lex() match {
      case Left(e) => {
        assert(e.isInstanceOf[ExpectedNumber])
        assertEquals("Expected number at position 2. Near 1-a", e.message)
      }
      case Right(value) => fail("Expected to fail with error")
    }
  }

  test("it handles skips extra whitespace") {
    val l = Lexer.make("2     13")
    val expectedOut: AbstractCronFragmentList = ListBuffer(Single("2", 2), Single("13", 13))
    l.lex() match {
      case Left(e) => fail(s"Received unexpected error ${e.message}")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }

  test("it lexes a full cron example input") {
    val l = Lexer.make("*/15 8-17 10 1,6 *")
    val expectedOut: AbstractCronFragmentList = ListBuffer(
      Divisor("*/15", 15),
      CronRange("8-17", 8, 17),
      Single("10", 10),
      CronList("1,6", List(1, 6)),
      Wildcard("*")
    )

    l.lex() match {
      case Left(value) => fail(s"Expected to succeed. Got error ${value.message}")
      case Right(value) => {
        assertEquals(expectedOut, value)
      }
    }
  }
}
