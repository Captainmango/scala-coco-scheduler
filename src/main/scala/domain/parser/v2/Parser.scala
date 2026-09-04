package domain.parser.v2

object CronParserV2 {
  def parse(l: List[List[CronLexem]]): CronEither[List[CronOperation]] = {
    l.foldLeft(Right(List.empty): CronEither[List[CronOperation]]) { (accEither, lexemList) =>
      for {
        acc <- accEither
        op <- processLexemList(lexList = lexemList)
      } yield acc ++ List(op)
    }
  }

  private def processLexemList(lexList: List[CronLexem]): CronEither[CronOperation] = {
    lexList match {
      case List(Asterisk) => Right(Wildcard())
      case List(Asterisk, Slash, Number(operand)) => {
        for {
          v <- parseNumber(operand)
        } yield Divisor(v)
      }
      case List(Number(n1), Comma, Number(n2)) => {
        for {
          v1 <- parseNumber(n1)
          v2 <- parseNumber(n2)
        } yield CList(List(v1, v2).sorted)
      }
      case List(Number(n1), Dash, Number(n2)) => {
        for {
          v1 <- parseNumber(n1)
          v2 <- parseNumber(n2)
        } yield CRange(bottom = Math.min(v1, v2), top = Math.max(v1, v2))
      }
      case List(Number(n)) => {
        for {
          v <- parseNumber(n)
        } yield Single(v)
      }
      case _ =>
        Left(ExpressionInvalid(lexList.foldLeft("") { (acc, l) => acc ++ l.raw }))
    }
  }

  private def parseNumber(n: String): CronEither[Int] =
    n.toIntOption.toRight(UnprocessableNumber(n))
}
