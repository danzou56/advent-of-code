package dev.danzou.advent22

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

internal class Day16 : AdventTestRunner22() {
    override val timeout = Duration.ofSeconds(30)

    data class Valve(val name: String)

    class Volcano(
      val valves: Map<Valve, Int>,
      val tunnels: Map<Valve, Set<Valve>>,
    ) {
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

    override fun part1(input: String): Int {
        val LIMIT = 30
        // Threshold at which we consider _not_ opening a valve - that is, it is more beneficial to
        // skip the valve and open the next one if any neighbor is NEIGHBOR_THRESHOLD times larger
        // than the current. Not formally checked, but it intuitively makes sense.
        val NEIGHBOR_THRESHOLD = 2
        val volcano = Volcano.fromString(input)
        // To find "maximal path", we subtract the actual cost from the total weight of all valves
        // releasing at once. Once all valves are opened, then the cost is zero, but while valves
        // are closed, it's advantageous to open them, that is, the cost decreases. This value (M)
        // makes the path satisfying max_p { Σ_p p[i] } the same as p for min_p { Σ_p (M - p[i]) }
        // where M - p[i] >= 0
        val costCeiling = volcano.valves.map { it.value }.sum()
        val openable = volcano.valves.filter { it.value > 0 }.keys

        data class State(val time: Int, val valve: Valve, val opened: Set<Valve>, val releasing: Int)

        val path = doDijkstras(
            init = State(0, Valve("AA"), emptySet(), 0),
            target = { (t) -> t >= LIMIT },
            getNeighbors = { (t, valve, opened, releasing) ->
                val nexts by lazy {
                    volcano.tunnels[valve]!!.map { State(t + 1, it, opened, releasing) }.toSet()
                }
                when {
                    opened.size == openable.size -> setOf(State(t + 1, valve, opened, releasing))
                    valve in openable && valve !in opened -> {
                        val openIt = State(
                            t + 1,
                            valve,
                            opened + valve,
                            releasing + volcano.valves[valve]!!
                        )
                        if (nexts.any { volcano.valves[it.valve]!! >= NEIGHBOR_THRESHOLD * volcano.valves[valve]!! })
                            nexts + openIt
                        else
                            setOf(openIt)
                    }

                    else -> nexts
                }
            },
            getCost = { _, (_, _, _, releasing) -> costCeiling - releasing }
        )

        // Pressure isn't generated until the _next_ step, so drop the last element of the path
        // before calculating its length
        return path.dropLast(1).sumOf { it.releasing }
    }

    override fun part2(input: String): Int {
        val LIMIT = 26
        val NEIGHBOR_THRESHOLD = 2
        val volcano = Volcano.fromString(input)
        val costCeiling = volcano.valves.map { it.value }.sum()
        val openable = volcano.valves.filter { it.value > 0 }.keys

        data class State(
          val time: Int,
          val selfValve: Valve,
          val elephantValve: Valve,
          val opened: Set<Valve>,
          val releasing: Int
        )

        fun nextSubStates(valve: Valve, nexts: Collection<Valve>, opened: Set<Valve>): Collection<Triple<Valve, Valve?, Int>> =
            when {
                valve in openable && valve !in opened -> {
                    val openIt = Triple(
                        valve,
                        valve,
                        volcano.valves[valve]!!
                    )
                    if (nexts.any { volcano.valves[it]!! >= NEIGHBOR_THRESHOLD * volcano.valves[valve]!! })
                        nexts.map { next ->
                            Triple(next, null, 0)
                        } + openIt
                    else
                        listOf(openIt)
                }
                else -> nexts.map { next ->
                    Triple(next, null, 0)
                }
            }

        val path = doDijkstras(
            init = State(0, Valve("AA"), Valve("AA"), emptySet(), 0),
            target = { (t) -> t >= LIMIT },
            getNeighbors = successors@{ (t, selfValve, elephantValve, opened, releasing) ->
                if (opened.size == openable.size)
                    return@successors setOf(State(t + 1, selfValve, elephantValve, opened, releasing))

                val nextSelfStates = (selfValve to volcano.tunnels[selfValve]!!).let { (valve, nexts) ->
                    nextSubStates(valve, nexts, opened)
                }
                val nextElephantStates = (elephantValve to volcano.tunnels[elephantValve]!!).let { (valve, nexts) ->
                    nextSubStates(valve, nexts, opened)
                }

                return@successors nextSelfStates.flatMap { (nextSelfValve, selfOpened, selfReleasing) ->
                    nextElephantStates.mapNotNull { (nextElephantValve, elephantOpened, elephantOpening) ->
                        if (selfOpened != null && elephantOpened != null && elephantOpened == selfOpened) null
                        else State(
                            t + 1,
                            nextSelfValve,
                            nextElephantValve,
                            opened + setOfNotNull(selfOpened, elephantOpened),
                            releasing + selfReleasing + elephantOpening
                        )
                    }
                }.toSet()
            },
            getCost = { _, (_, _, _, _, releasing) ->
                costCeiling - releasing
            }
        )

        return path.dropLast(1).sumOf { it.releasing }
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

        assertEquals(1651, part1(input))
        assertEquals(1707, part2(input))
    }
}