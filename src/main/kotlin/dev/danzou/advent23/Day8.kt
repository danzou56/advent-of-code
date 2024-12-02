package dev.danzou.advent23

import dev.danzou.advent.utils.lcm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day8 : AdventTestRunner23("Haunted Wasteland") {

    fun getNetwork(input: String): Map<String, List<String>> =
        input.split("\n").drop(2)
            .map { Regex("[A-Z0-9]{3}").findAll(it).map { it.value }.toList() }
            .associate { (src, dst1, dst2) -> src to listOf(dst1, dst2) }

    fun findPathLength(
        instrs: List<Char>,
        network: Map<String, List<String>>,
        start: String,
        end: (String) -> Boolean
    ): Int {
        tailrec fun run(cur: String, index: Int = 0): Int {
            if (end(cur)) return index
            return run(
                network[cur]!![when (instrs[index % instrs.size]) {
                    'L' -> 0
                    else -> 1
                }], index + 1
            )
        }

        return run(start)
    }

    override fun part1(input: String): Int {
        val instrs = input.takeWhile { it != '\n' }.toList()
        val network = getNetwork(input)
        return findPathLength(instrs, network, "AAA", { it == "ZZZ" })
    }

    override fun part2(input: String): Long {
        val instrs = input.takeWhile { it != '\n' }.toList()
        val network = getNetwork(input)
        return network.keys
            .filter { it.last() == 'A' }
            .map { findPathLength(instrs, network, it, { it.last() == 'Z' }) }
            .map { it.toLong() }
            .reduce(::lcm)
    }

    @Test
    fun testPart1Example() {
        val input = """
            RL

            AAA = (BBB, CCC)
            BBB = (DDD, EEE)
            CCC = (ZZZ, GGG)
            DDD = (DDD, DDD)
            EEE = (EEE, EEE)
            GGG = (GGG, GGG)
            ZZZ = (ZZZ, ZZZ)
        """.trimIndent()

        assertEquals(2, part1(input))
    }

    @Test
    fun testPart2Example() {
        val input = """
            LR

            11A = (11B, XXX)
            11B = (XXX, 11Z)
            11Z = (11B, XXX)
            22A = (22B, XXX)
            22B = (22C, 22C)
            22C = (22Z, 22Z)
            22Z = (22B, 22B)
            XXX = (XXX, XXX)
        """.trimIndent()

        assertEquals(6L, part2(input))
    }
}