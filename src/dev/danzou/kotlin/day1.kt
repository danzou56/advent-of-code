package dev.danzou.kotlin

import dev.danzou.utils.Utils.readInput
import kotlin.test.assertEquals

fun main() {
    val lines = readInput()

    val sum = lines.joinToString(",")
        .split(",,")
        .map { it.split(",").map { it.toLong() } }
        .map { it.sum() }
        .sorted()
        .takeLast(3)
        .sum()

    println(sum)

    assertEquals(0, 0) // Part 1
    assertEquals(209481, sum) // Part 2
}