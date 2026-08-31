package domain.parser

object Lexer {
    def make(input: String): Lexer = {
        new Lexer(_input = input)
    }
}

class Lexer(
    private var _input: String,
    private var readPos: Int = 0,
    private var currPos: Int = 0
) {
    def input_=(newValue: String): Unit = {
        readPos = 0
        currPos = 0
        _input = newValue
    }

    def input(): String = _input
}