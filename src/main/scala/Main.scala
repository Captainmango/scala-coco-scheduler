import domain.formatter.CronFormatter
import domain.parser.v1.given
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
      case Right(value) => println(CronFormatter.presentPossibleValues(value))
      case Left(value)  => println(value.toString())
    }
  }

  def main(args: Array[String]): Unit = ArgsParse(this).runOrExit(args)
}
