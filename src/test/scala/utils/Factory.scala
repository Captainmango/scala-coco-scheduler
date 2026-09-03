package utils

import domain.parser.v1.{AbstractCronFragmentList, CronList, CronRange, Divisor, Single, Wildcard}

import scala.collection.mutable.ListBuffer

object AbstractCronListFactory {
  def empty(): AbstractCronFragmentList =
    ListBuffer()

  def basic(): AbstractCronFragmentList =
    ListBuffer(
      Wildcard("*"),
      Wildcard("*"),
      Wildcard("*"),
      Wildcard("*"),
      Wildcard("*")
    )

  def realistic(): AbstractCronFragmentList =
    ListBuffer(
      new Divisor("*/15", 15),
      new CronRange("7-17", 7, 17),
      new CronList("1,12", List(1, 12)),
      new Wildcard("*"),
      new Single("1", 1)
    )
}
