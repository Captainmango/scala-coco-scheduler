package utils

import domain.parser.AbstractCronFragmentList
import scala.collection.mutable.ListBuffer
import domain.parser.Wildcard
import domain.parser.Divisor
import domain.parser.CronRange
import domain.parser.CronList
import domain.parser.Single

object AbstractCronListFactory {
    def empty(): AbstractCronFragmentList = 
        ListBuffer()
    
    def basic(): AbstractCronFragmentList = 
        ListBuffer(
            Wildcard("*"),
            Wildcard("*"),
            Wildcard("*"),
            Wildcard("*"),
            Wildcard("*"),
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
