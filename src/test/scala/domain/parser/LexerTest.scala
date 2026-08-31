package domain.parser

class LexerTest extends munit.FunSuite {
    test("it can reset the input") {
        val l = Lexer.make("test-input")
        val currentInput = l.input()

        val expectedInput = "new-input"

        l.input = expectedInput
        assertNotEquals(currentInput, l.input())
        assertEquals(l.input(), expectedInput)
    }
}
