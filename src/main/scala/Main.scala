import domain.formatter.CronFormatterV2
import domain.parser.v2.given
import domain.parser.{_, given}
import mainargs.{Parser => ArgsParse, arg, main}

object Main {
  @main
  def run(
      @arg("cron", doc = "The cron to parse")
      c: String
  ): Unit = {
    val res = parseCron(c)
    res match {
      case Right(value) => println(CronFormatterV2.presentPossibleValues(value))
      case Left(value)  => println(value.message)
    }
  }

  def main(args: Array[String]): Unit = ArgsParse(this).runOrExit(args)
}
