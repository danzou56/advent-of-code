package dev.danzou.advent22.kotlin

import dev.danzou.advent22.AdventTestRunner22

internal class Day3 : AdventTestRunner22() {
    fun priorityOf(char: Char): Int =
        if (char.isUpperCase()) char - 'A' + 27
        else char - 'a' + 1

    override fun part1(input: String): Number {
        return input.split("\n")
            .map { it.toList() }
            .map { it.chunked(it.size / 2).reduce(Iterable<Char>::intersect) }
            .map { priorityOf(it.first()) }
            .sum()
    }

    override fun part2(input: String): Number {
        return input.split("\n")
            .map { it.toList() }
            .chunked(3)
            .map { it.reduce(Iterable<Char>::intersect) }
            .map { priorityOf(it.first()) }
            .sum()
    }
}