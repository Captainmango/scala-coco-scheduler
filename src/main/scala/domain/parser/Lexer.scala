package domain.parser

import scala.collection.mutable.ListBuffer

val EOC = 'E'

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

    def lex(): Either[ParserError, AbstractCronFragmentList] = {
        val bounds = _input.length()
        var out = ListBuffer[AbstractCronFragment]()

        while (currPos < bounds) {
            val token = getCurrToken()

            val frag = token match {
                case '*' => handleAsterisk()
                case EOC => return Right(out)
                case _: Char => Left(InvalidInput(readRawInput(), readPos))
            }

            frag match {
                case Left(err) => return Left(err)
                case Right(cf) => out += cf
            }

            syncCurrAndReadPos()
            skipWhitespace()
            syncCurrAndReadPos()
        }

        return Right(out)
    }

    private def handleAsterisk(): Either[ParserError, AbstractCronFragment] = {
        incrementReadPos()
        var nextToken = getReadToken()

        nextToken match {
            case '/' => {
                incrementReadPos()
                nextToken = getReadToken()
                val cf = if (!nextToken.isDigit) then Left(ExpectedNumber(readRawInput(), readPos))
                else {
                    val num = readNumber()
                    Right(Divisor(readRawInput(), num))
                }
                incrementReadPos()
                cf
            }
            case ' ' | EOC => {
                val cf = Right(Wildcard(_input.slice(currPos, readPos)))
                incrementReadPos()
                cf
            }
            case _ => Left(InvalidInput(readRawInput(), readPos))
        }
    }

    private def getCurrToken(): Char = 
        if currPos >= _input.length then EOC
        else _input.charAt(currPos)

    private def getReadToken(): Char =
        if readPos >= _input.length() then EOC
        else _input.charAt(readPos)

    private def incrementCurrPos(by: Int = 1) = 
        currPos += by

    private def incrementReadPos(by: Int = 1) = 
        readPos += by

    private def syncCurrAndReadPos() = {
        val pos = readPos
        currPos = pos
        readPos = pos
    }

    private def readRawInput(): String = _input.slice(currPos, readPos+1)

    private def readNumber(): Int = {
        val tempReadPos = readPos
        while (getReadToken().isDigit) {
            incrementReadPos()
        }
        (_input.slice(tempReadPos, readPos)).toInt
    }

    private def skipWhitespace(): Unit = {
        var currToken = getCurrToken()
        while (currToken.isWhitespace) {
            incrementCurrPos()
            currToken = getCurrToken()
        }
    }
}