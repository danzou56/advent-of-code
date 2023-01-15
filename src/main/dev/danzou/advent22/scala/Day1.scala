package dev.danzou.advent22.scala

import dev.danzou.advent22.AdventTestRunner22

object Day1 extends AdventTestRunner22 {
  override def part1(input: String): AnyRef = ???

  override def part2(input: String): Number = input
    .split("\n\n")
    .map(_.split("\n").map(_.toLong))
    .map(_.sum)
    .sorted
    .takeRight(3)
    .sum
}