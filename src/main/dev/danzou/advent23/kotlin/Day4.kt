package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.pow
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day4 : AdventTestRunner23("Scratchcards") {
    // There's not actually a need to store winners and hand - both parts only
    // need to know the size of their intersection
    data class Game(val id: Int, val winners: Set<Int>, val hand: Set<Int>)
    private fun getGames(input: String): List<Game> =
        input.split("\n")
            .map { it.split("|", ":") }
            .map { it.map { Regex("\\d+").findAll(it) } }
            .map { (id, winners, hand) ->
                Game(
                    id.single().value.toInt(),
                    winners.map { it.value.toInt() }.toSet(),
                    hand.map { it.value.toInt() }.toSet()
                )
            }

    override fun part1(input: String): Int =
        getGames(input).sumOf { (_, winners, hand) ->
            val overlap = hand.intersect(winners).size
            if (overlap >= 1) 2.pow(overlap - 1)
            else 0
        }

    override fun part2(input: String): Long {
        val games = getGames(input)
        val cards: Map<Int, Long> = games.fold(
            games.associate { it.id to 1L }
        ) { cards, (id, winners, hand) ->
            cards + (id + 1..id + hand.intersect(winners).size).map { next ->
                // Problem states "Cards will never make you copy a card past the end of the table"
                // Not-null op is always safe
                next to cards[next]!! + cards[id]!!
            }
        }
        return cards.values.sum()
    }

    @Test
    fun testExample() {
        val input = """
            Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
            Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
            Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
            Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
            Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
            Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
        """.trimIndent()

        assertEquals(30L, part2(input))
    }
}