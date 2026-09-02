import domain.parser.Parser
import domain.parser.Lexer
import domain.formatter.CronFormatter
import mainargs.{main, arg, Parser as ArgsParse}

object Main {
  @main
  def run(
    @arg("cron", doc = "The cron to parse")
    c: String
  ): Unit =
    val parser = new Parser(Lexer.make(""))
    val result = for {
      c <- parser.parse(c)
    } yield c

    println(CronFormatter.presentPossibleValues(result.right.get))
  
  def main(args: Array[String]): Unit = ArgsParse(this).runOrExit(args)
}
