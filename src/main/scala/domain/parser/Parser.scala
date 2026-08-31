package domain.parser

class Parser (
    private val lexer: Lexer
) {
  private val cronIntervalOrder = List(
    CronInterval.MINUTE,
    CronInterval.HOUR,
    CronInterval.DAY_OF_MONTH,
    CronInterval.MONTH,
    CronInterval.WEEKDAY,
  )

  def parse(input: String): Either[ParserError, Cron] = {
    lexer.input = input
    val result = lexer.lex() match {
      case Left(value) => return Left(value)
      case Right(value) => value
    }

    if (result.length != cronIntervalOrder.length) then return Left(CronInvalid(input))
    val items = result.zip(cronIntervalOrder).map((acf, ci) => {
      val boundsCheck: (p: Int) => Boolean = (p => (ci.lowerBound to ci.upperBound).contains(p))

      acf match {
        case CronList(_, items) if (items.forall(boundsCheck)) => CronFragment(ci, acf)
        case Divisor(_, operand) if boundsCheck(operand) => CronFragment(ci, acf)
        case Range(_, bottom, top) if boundsCheck(bottom) && boundsCheck(top) => CronFragment(ci, acf)
        case Single(_, value) if boundsCheck(value) => CronFragment(ci, acf)
        case Wildcard(_) => CronFragment(ci, acf)
        case _ => throw new RuntimeException("oops")
      }
    })

    Right(Cron.fromCronFragments(items))
  }
}
