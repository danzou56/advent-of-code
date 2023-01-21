package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day16 : AdventTestRunner22() {
    data class Valve(val name: String)

    class Volcano(
        val valves: Map<Valve, Int>,
        val tunnels: Map<Valve, Set<Valve>>,
    ) {
        fun pressureReleasedFrom(path: List<Valve>): Int {
            require(path.isNotEmpty()) { "path must be non empty" }
            require(path.first() == Valve("AA")) { "path must start with ${Valve("AA")}" }

            // weird thing here: the list produced should have an extra zero at
            // and one fewer number at the back. This is because the pressure
            // released by opening a valve isn't produced until the **next**
            // step and not the current one
            return path.windowed(2).also { assert(it.size == 30) }.fold(Pair(listOf(0), emptySet<Valve>())) { (pressures, opened), (prev, next) ->
                if (prev == next && prev !in opened) Pair(pressures + (pressures.last() + valves[prev]!!), opened + prev)
                else Pair(pressures + pressures.last(), opened)
            }.first.dropLast(1).sum()
        }

        companion object {
            fun fromString(input: String): Volcano {
                val entries = input.split("\n")
                    .map { line ->
                        val valves = Regex("""[A-Z]{2}""").findAll(line).map { Valve(it.value) }
                        val flows = Regex("""\d+""").findAll(line).map { it.value.toInt() }
                        Triple(valves.first(), flows.first(), valves.drop(1))
                    }
                return Volcano(
                    valves = entries.associate { it.first to it.second },
                    tunnels = entries.associate { it.first to it.third.toSet() },
                )
            }
        }
    }

    override fun part1(input: String): Any {
        TODO("Not yet implemented")
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
        """.trimIndent()

        val volcano = Volcano.fromString(input)
        val expectedPath = """
            AA
            DD
            DD
            CC
            BB
            BB
            AA
            II
            JJ
            JJ
            II
            AA
            DD
            EE
            FF
            GG
            HH
            HH
            GG
            FF
            EE
            EE
            DD
            CC
            CC
            CC
            CC
            CC
            CC
            CC
            CC
        """.trimIndent().split("\n").map { Valve(it) }

        assertEquals(31, expectedPath.size)
        assertEquals(1651, volcano.pressureReleasedFrom(expectedPath))

        assertEquals(1651, part1(input))
    }
}