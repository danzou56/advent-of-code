package dev.danzou.advent22.kotlin

import dev.danzou.advent22.AdventTestRunner22

internal class Day1 : AdventTestRunner22() {
    override fun part1(input: String): Number =
        input.split("\n\n")
            .map { it.split("\n").map { it.toLong() } }
            .map { it.sum() }
            .max()

    override fun part2(input: String): Number =
        input.split("\n\n")
            .map { it.split("\n").map { it.toLong() } }
            .map { it.sum() }
            .sorted()
            .takeLast(3)
            .sum()
}