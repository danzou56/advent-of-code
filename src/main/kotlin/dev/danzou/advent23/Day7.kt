package dev.danzou.advent23

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day7 : AdventTestRunner23("Camel Cards") {
    // We want what is essentially a modified lexicographic order. Since this is already achieved
    // when comparing Strings, remap the string to make the standard lexicographic comparison can
    // be applied. That is, the hand 9TJQKA becomes hijkl which can be sorted as normal.
    val cardValues = "A, K, Q, J, T, 9, 8, 7, 6, 5, 4, 3, 2".split(", ")
        .map { it.single() }
        .reversed()
        .mapIndexed{ i, c -> c to 'a' + i }
        .toMap()

    fun rank(hand: Map<Char, Int>): Int {
        return when {
            hand.values.any { it == 5 } -> 6
            hand.values.any { it == 4 } -> 5
            hand.values.count { it == 3 } == 1 && hand.values.count { it == 2 } == 1 -> 4
            hand.values.any { it == 3 } -> 3
            hand.values.count { it == 2 } == 2 -> 2
            hand.values.any { it == 2 } -> 1
            hand.values.count { it == 1 } == 5 -> 0
            else -> throw IllegalArgumentException("Illegal hand")
        }
    }

    override fun part1(input: String): Long {
        return input.split("\n")
            .map { it.split(" ") }
            .sortedBy { (hand, _) -> hand.map(cardValues::getValue).joinToString("") }
            .sortedBy { (hand, _) -> rank(hand.groupingBy { it }.eachCount()) }
            .mapIndexed { i, (_, winnings) ->
                (i + 1) * winnings.toLong()
            }
            .sum()
    }

    override fun part2(input: String): Long {
        val cardValues = cardValues + ('J' to cardValues['2']!! - 1)

        fun maximizedRank(hand: Map<Char, Int>): Int {
            return if ('J' in hand && hand.keys.size > 1) {
                val maxCard = hand.filter { (card, _) -> card != 'J' }.maxBy { it.value }.key
                rank(hand.mapValues { (k, v) ->
                    when (k) {
                        maxCard -> v + hand['J']!!
                        'J' -> 0
                        else -> v
                    }
                })
            } else rank(hand)
        }

        return input.split("\n")
            .map { it.split(" ") }
            .sortedBy { (hand, _) -> hand.map(cardValues::getValue).joinToString("") }
            .sortedBy { (hand, _) -> maximizedRank(hand.groupingBy { it }.eachCount()) }
            .mapIndexed { i, (_, winnings) ->
                (i + 1) * winnings.toLong()
            }
            .sum()
    }

    @Test
    fun testExample() {
        val input = """
            32T3K 765
            T55J5 684
            KK677 28
            KTJJT 220
            QQQJA 483
        """.trimIndent()

        assertEquals(6440L, part1(input))
        assertEquals(5905L, part2(input))
    }
}