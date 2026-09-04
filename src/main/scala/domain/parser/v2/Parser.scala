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
        operand.toIntOption match {
          case Some(value) => Right(Divisor(value))
          case None        => Left(UnprocessableNumber(operand))
        }
      }
      case List(Number(n1), Comma, Number(n2)) => {
        n1.toIntOption match
          case Some(v1) =>
            n2.toIntOption match {
              case Some(v2) => Right(CList(List(v1, v2).sorted))
              case None     => Left(UnprocessableNumber(n2))
            }
          case None => Left(UnprocessableNumber(n1))
      }
      case List(Number(n1), Dash, Number(n2)) => {
        n1.toIntOption match
          case Some(v1) =>
            n2.toIntOption match {
              case Some(v2) => Right(CRange(bottom = Math.min(v1, v2), top = Math.max(v1, v2)))
              case None     => Left(UnprocessableNumber(n2))
            }
          case None => Left(UnprocessableNumber(n1))
      }
      case List(Number(n)) => {
        n.toIntOption match {
          case Some(value) => Right(Single(value))
          case None        => Left(UnprocessableNumber(n))
        }
      }
      case _ =>
        Left(ExpressionInvalid(lexList.foldLeft("") { (acc, l) =>
          acc ++ l.raw
        }))
    }
  }
}
