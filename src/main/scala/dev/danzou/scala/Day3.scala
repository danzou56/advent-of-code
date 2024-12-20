package dev.danzou.scala

import dev.danzou.advent22.AdventTestRunner22

class Day3 extends AdventTestRunner22 {
  private def priorityOf(char: Char): Int = {
    if (char.isUpper) char - 'A' + 27
    else char - 'a' + 1
  }

  override def part1(input: String): Number =
    input.split("\n")
      .map(str => str.toList)
      .map(list =>
        list.slice(0, list.size / 2).intersect(list.slice(list.size / 2, list.size)))
      .map(list => priorityOf(list.head))
      .sum

  override def part2(input: String): Number = ???
}
