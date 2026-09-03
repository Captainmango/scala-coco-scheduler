package utils

import domain.parser.{Cron, CronEither, CronParserAlg, Monadish}

case class TestResult[A](input: String, output: CronEither[A])

def testInterpreter(expected: CronEither[Cron]): CronParserAlg[TestResult] =
  new CronParserAlg[TestResult] {
    def parse(input: String): TestResult[Cron] = TestResult(input, expected)
  }

given MonadishTestResult: Monadish[TestResult] with {
  override def pure[A](a: A): TestResult[A] = TestResult("", Right(a))
  override def flatMap[A, B](fa: TestResult[A])(f: A => TestResult[B]): TestResult[B] =
    fa.output match {
      case Right(value) => f(value)
      case Left(value)  => TestResult(fa.input, Left(value))
    }
}
