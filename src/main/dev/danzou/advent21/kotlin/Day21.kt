package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.assertEquals

internal class Day21 : AdventTestRunner21("Dirac Dice") {

    private fun String.getPlayer1(): Int =
        this.split("\n").first().last().digitToInt()

    private fun String.getPlayer2(): Int =
        this.split("\n").last().last().digitToInt()

    override fun part1(input: String): Int {
        val deterministicDice = object : Iterator<Triple<Int, Int, Int>> {
            private var roll = 1
            var rolls = 0

            override fun hasNext(): Boolean = true
            override fun next(): Triple<Int, Int, Int> {
                val roll = Triple(roll, roll % 100 + 1, (roll + 1) % 100 + 1)
                this.roll = (this.roll + 2) % 100 + 1
                this.rolls += 3
                return roll
            }
        }

        val losingScore = run play@{
            deterministicDice.asSequence().fold(
                // player 1 pos, score, player 2 pos, score
                listOf(input.getPlayer1(), 0, input.getPlayer2(), 0)
            ) { (curPlayerSpace, curPlayerScore, nextPlayerSpace, nextPlayerScore), (r1, r2, r3) ->
                val curPlayerNextSpace = (curPlayerSpace + r1 + r2 + r3 - 1) % 10 + 1
                val curPlayerNextScore = curPlayerScore + curPlayerNextSpace
                if (curPlayerNextScore >= 1000) return@play nextPlayerScore
                listOf(nextPlayerSpace, nextPlayerScore, curPlayerNextSpace, curPlayerNextScore)
            }
            throw IllegalStateException("Fold on infinite iterator ended")
        }

        return losingScore * deterministicDice.rolls
    }

    override fun part2(input: String): Long {
        val threshold = 21
        val rollOutcomes = (1..3).flatMap { i ->
            (1..3).flatMap { j ->
                (1..3).map { k ->
                    i + j + k
                }
            }
        }.groupingBy { it }.eachCount()
        val cache = mutableMapOf<List<Int>, Pair<Long, Long>>()
        fun wins(curSpace: Int, curScore: Int, nextSpace: Int, nextScore: Int): Pair<Long, Long> {
            if (nextScore >= threshold) return Pair(0, 1)
            return rollOutcomes.map { (roll, count) ->
                val curNextSpace = (curSpace + roll - 1) % 10 + 1
                val curNextScore = curScore + curNextSpace
                cache.getOrPut(listOf(nextSpace, nextScore, curNextSpace, curNextScore)) {
                    wins(nextSpace, nextScore, curNextSpace, curNextScore)
                }.let { (w1, w2) -> Pair(w2 * count, w1 * count) }
            }.reduce(Pair<Long, Long>::plus)
        }

        val (p1wins, p2wins) = wins(input.getPlayer1(), 0, input.getPlayer2(), 0)
        return max(p1wins, p2wins)
    }

    @Test
    fun testExample() {
        val input = """
            Player 1 starting position: 4
            Player 2 starting position: 8
        """.trimIndent()

        assertEquals(739785, part1(input))
        assertEquals(444356092776315L, part2(input))
    }
}