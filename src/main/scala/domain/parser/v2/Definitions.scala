package domain.parser.v2

type CronEither = [A] =>> Either[ParserError, A]

trait Monadish[F[_]] {
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => pure(f(a)))
}

given MyMonad: Monadish[CronEither] with {
  override def pure[A](a: A): CronEither[A] = Right(a)
  override def flatMap[A, B](fa: CronEither[A])(f: A => CronEither[B]): CronEither[B] =
    fa.flatMap(f)
}

extension [F[_], A](fa: F[A])(using M: Monadish[F]) {
  def map[B](f: A => B): F[B] = M.map(fa)(f)
  def flatMap[B](f: A => F[B]): F[B] = M.flatMap(fa)(f)
}

trait CronLexerAlg[F[_]] {
  def lex(input: String): F[List[CronLexem]]
}

trait CronParserAlg[F[_]] {
  def parse(input: List[CronLexem]): F[List[CronOperation]]
}

trait CronEvaluatorAlg[F[_]] {
  def evaluate(input: List[CronOperation]): F[Cron]
}
