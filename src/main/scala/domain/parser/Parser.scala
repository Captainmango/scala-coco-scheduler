package domain.parser

given CronParser: CronParserAlg[CronEither] with {
  override def parse(input: String): CronEither[Cron] = {
    val lexer = Lexer.make(input)
    lexer.lex() match {
      case Right(value) => new CronValidator().validate(value)
      case Left(value)  => Left(value)
    }
  }
}
