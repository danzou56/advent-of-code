package dev.danzou.kotlin

import dev.danzou.kotlin.utils.AdventTestRunner
import org.junit.jupiter.api.Test

internal class Day2 : AdventTestRunner() {
    @Test
    override fun part1() {
        fun getScore(opponent: String, you: String): Int {
            val scores = mapOf("X" to 1, "Y" to 2, "Z" to 3)
            val score = if (opponent == "A" && you == "X" ||
                    opponent == "B" && you == "Y" ||
                    opponent == "C" && you == "Z") scores[you]!! + 3
            else if (opponent == "A" && you == "Z" ||
                opponent == "B" && you == "X" ||
                opponent == "C" && you == "Y") scores[you]!!
            else scores[you]!! + 6
            println(score)
            return score
        }

        val rows = inputLines.map { it.split(" ") }
            .map { getScore(it.component1(), it.component2()) }

        println(rows.sum())
    }

    @Test
    override fun part2() {
        fun getScore(opponent: String, you: String): Int {
            // "X" is lose, "Y" is draw, "Z" is win

            val scores = mapOf("A" to 1, "B" to 2, "C" to 3)
            val loser = mapOf("A" to "C", "B" to "A", "C" to "B")
            val winner = mapOf("C" to "A", "A" to "B", "B" to "C")

            if (you == "X") {
                return 0 + scores[loser[opponent]!!]!!
            } else if (you == "Y") {
                return 3 + scores[opponent]!!
            } else {
                assert(you == "Z")
                return 6 + scores[winner[opponent]!!]!!
            }
        }

        val rows = inputLines.map { it.split(" ") }
            .map { getScore(it.component1(), it.component2()) }

        println(rows.sum())
    }
}