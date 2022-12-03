package dev.danzou.kotlin

import dev.danzou.kotlin.utils.AdventTestRunner

internal class Day2 : AdventTestRunner() {
    override fun part1(input: String): Number {
        fun getScore(opponent: String, you: String): Int {
            val scores = mapOf("X" to 1, "Y" to 2, "Z" to 3)
            val modToScore = mapOf(0 to 3, 1 to 0, 2 to 6)
            return scores[you]!! + modToScore[((opponent[0] + ('X' - 'A')) - you[0]).mod(3)]!!
        }

        val rows = input.split("\n").map { it.split(" ") }
            .map { getScore(it.component1(), it.component2()) }

        return rows.sum()
    }

    override fun part2(input: String): Number {
        fun getScore(opponent: String, you: String): Int {
            // "X" is lose, "Y" is draw, "Z" is win
            val scores = mapOf("A" to 1, "B" to 2, "C" to 3)
            val winner = mapOf("C" to "A", "A" to "B", "B" to "C")
            val loser = winner.entries.associate { (k, v) -> v to k }

            return when (you) {
                "X" -> 0 + scores[loser[opponent]!!]!!
                "Y" -> 3 + scores[opponent]!!
                "Z" -> 6 + scores[winner[opponent]!!]!!
                else -> throw IllegalStateException()
            }
        }

        val rows = input.split("\n").map { it.split(" ") }
            .map { getScore(it.component1(), it.component2()) }

        return rows.sum()
    }
}