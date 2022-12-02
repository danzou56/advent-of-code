package dev.danzou.kotlin.utils

import dev.danzou.utils.Utils.readInputLines
import dev.danzou.utils.Utils.readInputString
import org.junit.jupiter.api.Test

abstract class AdventTestRunner {
    val inputLines: List<String> = readInputLines()
    val inputString: String = readInputString()

    @Test
    abstract fun part1()
    @Test
    abstract fun part2()
}