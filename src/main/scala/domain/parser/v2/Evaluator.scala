package domain.parser.v2

import domain.parser.v2.InvalidInput

object CronEvaluatorV2 {
  private val cronIntervalOrder = List(
    CronInterval.MINUTE,
    CronInterval.HOUR,
    CronInterval.DAY_OF_MONTH,
    CronInterval.MONTH,
    CronInterval.WEEKDAY
  )

  def evaluate(input: List[CronOperation]): CronEither[Cron] = {
    if (input.length != cronIntervalOrder.length) then Left(IncorrectExpressionLength(input.length))
    else {
      for {
        d <- processList(input)
        c <- Cron.fromFragmentList(d).toRight(InvalidInput())
      } yield c
    }
  }

  private def isInBounds(op: CronOperation, ci: CronInterval): Boolean =
    op match {
      case Wildcard()  => true
      case Single(num) => (ci.lowerBound to ci.upperBound).contains(num)
      case CRange(bottom, top) =>
        (ci.lowerBound to ci.upperBound).contains(bottom)
        && (ci.lowerBound to ci.upperBound).contains(top)
      case CList(items)     => items.forall(n => (ci.lowerBound to ci.upperBound).contains(n))
      case Divisor(operand) => (ci.lowerBound to ci.upperBound).contains(operand)
    }

  private def processList(input: List[CronOperation]): CronEither[List[CronFragment]] =
    input.zip(cronIntervalOrder).foldLeft(Right(List.empty): CronEither[List[CronFragment]]) { (accEither, pair) =>
      for {
        acc <- accEither
        frag <- processOperation(pair._1, pair._2)
      } yield acc ++ List(frag)
    }

  private def processOperation(op: CronOperation, ci: CronInterval): CronEither[CronFragment] = {
    if !isInBounds(op, ci) then Left(OutOfIntervalBounds(ci))
    else Right(CronFragment(ci, op))
  }
}
