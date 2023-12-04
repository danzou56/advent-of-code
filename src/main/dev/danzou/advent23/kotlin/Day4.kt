package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.pow
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day4 : AdventTestRunner23() {
    override fun part1(input: String): Int {
        return input.split("\n")
            .map { it.split("|") }
            .map { (winners, hand) ->
                val trueWinners = Regex("\\d+").findAll(winners)
                    .drop(1)
                    .map { it.value.toInt() }
                val hand = Regex("\\d+").findAll(hand)
                    .map { it.value.toInt() }
                val num = hand.count { it in trueWinners }
                if (num >= 1) 2.pow(num - 1)
                else 0
            }
            .sum()
    }

    override fun part2(input: String): Long {
        val i =  input.split("\n")
            .map { it.split("|") }
            .map { (winners, hand) ->
                val (id, trueWinners) = Regex("\\d+").findAll(winners)
                    .toList()
                    .let { results ->
                        Pair(results.first().value.toInt(), results.drop(1)
                            .map { it.value.toInt() })
                    }
                val hand = Regex("\\d+").findAll(hand)
                    .map { it.value.toInt() }
                val num = hand.count { it in trueWinners }
                Pair(id, num)
            }
        val cards: Map<Int, Long> = i.foldIndexed(
            i.associate { it.first to 1L }
        ) { i, cardMap, (curId, copiesWon) ->
            cardMap + ((curId + 1)..(curId + copiesWon)).map { nextId ->
                nextId to (cardMap.getOrDefault(nextId, 1) + cardMap.getOrDefault(curId, 1))
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