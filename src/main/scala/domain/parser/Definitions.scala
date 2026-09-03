package domain.parser

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

trait CronParserAlg[F[_]] {
  def parse(input: String): F[Cron]
}
