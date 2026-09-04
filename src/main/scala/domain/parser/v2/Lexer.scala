package domain.parser.v2

import scala.annotation.tailrec

object CronLexerV2 {
  def lex(input: String): CronEither[List[List[CronLexem]]] = {
    val segments = input.split("\\s+").filter(_.nonEmpty).toList

    segments.foldLeft(Right(List.empty): CronEither[List[CronLexem]]) { (accEither, seg) =>
      for {
        acc <- accEither
        lexems <- processSegment(seg)
      } yield acc ++ lexems.reverse
    } match {
      case Right(value) => Right(List(value))
      case Left(value)  => Left(value)
    }
  }

  private def processSegment(input: String): CronEither[List[CronLexem]] = {
    @tailrec
    def loop(idx: Int, acc: List[CronLexem]): CronEither[List[CronLexem]] = {
      if (idx >= input.length()) then Right(acc)
      else {
        input.charAt(idx) match {
          case '*' => loop(idx + 1, Asterisk :: acc)
          case '/' => loop(idx + 1, Slash :: acc)
          case '-' => loop(idx + 1, Dash :: acc)
          case ',' => loop(idx + 1, Comma :: acc)
          case c if c.isDigit => {
            val (digitsString, nextIdx) = readNumber(input, idx)
            loop(nextIdx, Number(digitsString) :: acc)
          }
          case _: Char => Left(InvalidInput())
        }
      }
    }

    def readNumber(input: String, startIdx: Int): (String, Int) = {
      var i = startIdx
      while (i < input.length() && input.charAt(i).isDigit) i += 1
      (input.slice(startIdx, i), i)
    }

    loop(0, Nil)
  }
}
