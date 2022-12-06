package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner

internal class Day6 : AdventTestRunner() {
    override fun part1(input: String): Any =
        4.let { windowSize ->
            input.windowed(windowSize).indexOfFirst { it.toSet().size == windowSize } + windowSize
        }

    override fun part2(input: String): Any =
        14.let { windowSize ->
            input.windowed(windowSize).indexOfFirst { it.toSet().size == windowSize } + windowSize
        }
}