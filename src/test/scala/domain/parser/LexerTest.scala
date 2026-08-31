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
            case Left(e) => fail(s"Recieved unexpected error $e}")
            case Right(out) => out
        }

        assertEquals(expectedOut, res)
    }

    test("it can lex two operators") {
        val l = Lexer.make("* */15")
        val expectedOut = ListBuffer[AbstractCronFragment](Wildcard("*"), Divisor("*/15", 15))

        val res = l.lex().getOrElse(())
        assertEquals(expectedOut, res)
    }

    test("it errors if input isn't correct") {
        val l = Lexer.make("*a")

        l.lex() match {
            case Left(value) => {
                assert(value.isInstanceOf[InvalidInput])
                assertEquals("Invalid input at 1. Near *a", value.toString())
            }
            case Right(value) => fail("Should have failed with error")
        }
    }

    test("it errors if it cannot read number where a number should be") {
        val l = Lexer.make("*/ddd")

        l.lex() match {
            case Left(value) => {
                assert(value.isInstanceOf[ExpectedNumber])
                assertEquals("Expected number at 2. Near */d", value.toString())
            }
            case Right(value) => fail("Should have failed with error")
        }
    }

    test("it errors if not valid input") {
        val l = Lexer.make("d")

        l.lex() match {
            case Left(value) => {
                assert(value.isInstanceOf[InvalidInput])
                assertEquals("Invalid input at 0. Near d", value.toString())
            }
            case Right(value) => fail("Should have failed with error")
        }
    }
}
