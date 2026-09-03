package domain.parser

given CronParser: CronParserAlg[CronEither] with {
  override def parse(input: String): CronEither[Cron] = {
    val lexer = Lexer.make(input)
    val acl = lexer.lex() match {
      case Right(v)        => v // Success means continue
      case l @ Left(value) => return l.asInstanceOf[CronEither[Cron]]
    }

    val validator = new CronValidator
    validator.validate(acl)
  }
}
