package dev.danzou.kotlin

import dev.danzou.utils.Utils.readInputString
import kotlin.test.assertEquals

fun main() {
    val input = readInputString()

    val sums = input
        .split("\n\n")
        .map { it.split("\n").map { it.toLong() } }
        .map { it.sum() }

    val part1 = sums.max()

    println("Part 1: $part1")

    val part2 = sums
        .sorted()
        .takeLast(3)
        .sum()

    println("Part 2: $part2")
    assertEquals(209481, part2) // Part 2
}