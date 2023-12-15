package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21

typealias Digit = Set<Char>

internal class Day8 : AdventTestRunner21() {

    class IntToReal(var data: MutableMap<Int, Digit>) {
        operator fun get(num: Int): Digit = this.data[num]!!
        operator fun set(num: Int, set: Digit) = this.data.set(num, set)
    }

    data class Entry(val patterns: List<Digit>, val output: List<Digit>)

    private fun parseInput(input: String): List<Entry> =
        input.split("\n")
            .map {
                it.split(" | ")
                    .map { it.split(" ") }
            }
            .map { Entry(it[0].map { it.toSet() }, it[1].map { it.toSet() }) }

    override fun part1(input: String): Int =
        parseInput(input).map(Entry::output)
            .sumOf { outputs ->
                outputs.count {
                    it.size in setOf(2, 3, 4, 7)
                }
            }

    override fun part2(input: String): Int {
        val entries = parseInput(input)
        val patternList = entries.map(Entry::patterns)
        val outputList = entries.map(Entry::output)
        val intToReals = patternList.map { patterns ->
            IntToReal(
                mutableMapOf(
                    1 to patterns.first { it.size == 2 },
                    7 to patterns.first { it.size == 3 },
                    4 to patterns.first { it.size == 4 },
                    8 to patterns.first { it.size == 7 },
                )
            )
        }

        for ((patterns, intToReal) in patternList.zip(intToReals)) {
            intToReal[3] = patterns.first { it.size == 5 && it.containsAll(intToReal[1]) }
            intToReal[9] = patterns.first { it.size == 6 && it.containsAll(intToReal[3]) }
            intToReal[6] = patterns.first { it.size == 6 && it != intToReal[9] && !it.containsAll(intToReal[7]) }
            intToReal[0] = patterns.first { it.size == 6 && it != intToReal[9] && it != intToReal[6] }
            intToReal[5] = patterns.first { it.size == 5 && intToReal[6].containsAll(it) }
            intToReal[2] = patterns.first { it.size == 5 && it != intToReal[5] && it != intToReal[3] }
        }

        return outputList.zip(intToReals).sumOf { (outputs, intToReal) ->
            val realToInt = intToReal.data.entries.associateBy({ it.value }) { it.key }
            outputs.map { realToInt[it] }.joinToString(separator = "") { digit -> digit.toString() }.toInt()
        }
    }
}