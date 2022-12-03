package dev.danzou.scala

import dev.danzou.utils.Utils.readInputString

object Day1 {
  def main(args: Array[String]): Unit = {
    val input = readInputString()

    val sum = input
      .split("\n\n")
      .map({ _.split("\n").map({ _.toLong }) })
      .map({ _.sum })
      .sorted
      .takeRight(3)
      .sum

    println(sum)

    assert(209481 == sum)
  }
}