package dev.danzou.advent21

internal class Day1 : AdventTestRunner21() {
    override fun part1(input: String): Int =
        input.split("\n")
            .map(String::toInt)
            .windowed(2)
            .count { it.last() > it.first() }

    override fun part2(input: String): Int =
        input.split("\n")
            .map(String::toInt)
            .windowed(3)
            .map { it.sum() }
            .windowed(2)
            .count { it.last() > it.first() }

}