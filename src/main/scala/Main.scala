import domain.formatter.CronFormatter
import domain.parser.{CronParser, Lexer}
import mainargs.{Parser => ArgsParse, arg, main}

object Main {
  @main
  def run(
      @arg("cron", doc = "The cron to parse")
      c: String
  ): Unit =
    val lexer = Lexer.make(c)
    val parser = new CronParser

    val result = for {
      acl <- lexer.lex()
      c <- parser.parse(acl)
    } yield c

    result match {
      case Right(value) => println(CronFormatter.presentPossibleValues(value))
      case Left(value)  => println(value.toString())
    }

  def main(args: Array[String]): Unit = ArgsParse(this).runOrExit(args)
}
