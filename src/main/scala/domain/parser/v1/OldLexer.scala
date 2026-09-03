package domain.parser.v1

import domain.parser._

import scala.collection.mutable.ListBuffer
import scala.util.boundary.break
import scala.util.{Failure, Success, Try, boundary}

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
      var token = getCurrToken() match {
        case Some(value) => value
        case None        => EOC
      }

      val frag = token match {
        case '*'            => handleAsterisk()
        case c if c.isDigit => handleDigit()
        case EOC            => return Right(out)
        case _: Char        => Left(InvalidInput(readRawInput(), readPos))
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
    var nextToken = getReadToken() match
      case Some(value) => value
      case None        => EOC

    nextToken match {
      case '/' => {
        incrementReadPos()
        nextToken = getReadToken() match
          case Some(value) => value
          case None        => EOC

        val cf =
          if (!nextToken.isDigit) then Left(ExpectedNumber(readRawInput(), readPos))
          else {
            for {
              num <- readNumber()
            } yield Divisor(readRawInput(), num)
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

  private def handleDigit(): Either[ParserError, AbstractCronFragment] = boundary {
    readNumber().flatMap({ num =>
      var currToken = getReadToken() match
        case Some(value) => value
        case None        => EOC

      currToken match {
        case ' ' | EOC => {
          val cf = Single(readRawInput(), num)
          incrementReadPos()
          Right(cf)
        }
        case '-' => {
          incrementReadPos()
          currToken = getReadToken() match
            case Some(value) => value
            case None        => EOC

          if (!currToken.isDigit) then break(Left(ExpectedNumber(readRawInput(), readPos)))
          val cf = for {
            topOfRange <- readNumber()
          } yield CronRange(readRawInput(), bottom = num, top = topOfRange)
          incrementReadPos()
          cf
        }
        case ',' => {
          incrementReadPos()
          currToken = getReadToken() match
            case Some(value) => value
            case None        => EOC

          if (!currToken.isDigit) then break(Left(ExpectedNumber(readRawInput(), readPos)))
          val cf = for {

            numTwo <- readNumber()
          } yield CronList(readRawInput(), List(num, numTwo))
          incrementReadPos()
          cf
        }
        case _: Char => Left(InvalidInput(readRawInput(), readPos))
      }
    })
  }

  private def getCurrToken(): Option[Char] =
    if currPos >= _input.length then None
    else Some(_input.charAt(currPos))

  private def getReadToken(): Option[Char] =
    if readPos >= _input.length() then None
    else Some(_input.charAt(readPos))

  private def incrementCurrPos(by: Int = 1) =
    currPos += by

  private def incrementReadPos(by: Int = 1) =
    readPos += by

  private def syncCurrAndReadPos() =
    if (readPos > currPos) {
      currPos = readPos
    } else {
      readPos = currPos
    }

  private def readRawInput(): String = _input.slice(currPos, readPos + 1).trim()

  private val operators = Set('*', '/', '-', ',')

  private def isOperator(c: Char): Boolean = operators.contains(c)

  private def readNumber(): Either[ParserError, Int] = {
    val tempReadPos = readPos
    var tok = getReadToken() match
      case Some(value) => value
      case None        => EOC

    while (!tok.isWhitespace && !isOperator(tok) && tok != EOC) {
      incrementReadPos()
      tok = getReadToken() match
        case Some(value) => value
        case None        => EOC
    }

    Try((_input.slice(tempReadPos, readPos)).toInt) match {
      case Success(value)     => Right(value)
      case Failure(exception) => Left(UnprocessableNumber(_input.slice(tempReadPos, readPos)))
    }
  }

  private def skipWhitespace(): Unit = {
    var currToken = getCurrToken() match
      case Some(value) => value
      case None        => EOC

    while (currToken.isWhitespace) {
      incrementCurrPos()
      currToken = getCurrToken() match
        case Some(value) => value
        case None        => EOC

    }
  }
}
