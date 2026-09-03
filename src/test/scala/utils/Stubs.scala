package utils

import domain.parser.{Cron, CronEither, Monadish}
import domain.parser.AbstractCronFragment
import domain.parser.CronLexerAlg
import domain.parser.CronValidatorAlg

enum Traces {
  case Lexed(input: String)
  case Validated(tokens: List[AbstractCronFragment])
}

case class TestResult[A](traces: List[Traces], output: CronEither[A])

def testValidator(expected: CronEither[Cron]): CronValidatorAlg[TestResult] =
  new CronValidatorAlg[TestResult] {
    def validate(input: List[AbstractCronFragment]): TestResult[Cron] =
      TestResult(List(Traces.Validated(input)), expected)
  }

def testLexer(expected: CronEither[List[AbstractCronFragment]]): CronLexerAlg[TestResult] =
  new CronLexerAlg[TestResult] {
    def lex(input: String): TestResult[List[AbstractCronFragment]] =
      TestResult(List(Traces.Lexed(input)), expected)
  }

given MonadishTestResult: Monadish[TestResult] with {
  override def pure[A](a: A): TestResult[A] = TestResult(List.empty, Right(a))
  override def flatMap[A, B](fa: TestResult[A])(f: A => TestResult[B]): TestResult[B] =
    fa.output match {
      case Right(value) =>
        val fb = f(value)
        TestResult(fa.traces ++ fb.traces, fb.output)
      case Left(value) => TestResult(fa.traces, Left(value))
    }
}
