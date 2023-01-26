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
            return path.windowed(2).fold(Pair(listOf(0), emptySet<Valve>())) { (pressures, opened), (prev, next) ->
                if (prev == next && prev !in opened) Pair(pressures + (pressures.last() + valves[prev]!!), opened + prev)
                else Pair(pressures + pressures.last(), opened)
            }.first.dropLast(1).sum()
        }

        fun pressureReleasedFrom(path: List<Valve>, elephantPath: List<Valve>): Int {
            return path.zip(elephantPath).windowed(2)
                .fold(Pair(listOf(0), emptySet<Valve>())) { (pressures, opened), (prev, next) ->
                    var pressure = pressures.last()
                    val valvesOpened = mutableSetOf<Valve>()
                    if (prev.first == next.first && prev.first !in opened) {
                        pressure += valves[prev.first]!!
                        valvesOpened += prev.first
                    }
                    if (prev.second == next.second && prev.second !in opened) {
                        pressure += valves[prev.second]!!
                        valvesOpened += prev.second
                    }

                    Pair(pressures + pressure, opened + valvesOpened)
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
        val limit = 30
        val volcano = Volcano.fromString(input)
        val costCeiling = volcano.valves.map { limit * it.value }.max()
        val openable = volcano.valves.filter { it.value > 0 }.keys

        data class State(val time: Int, val valve: Valve, val opened: Set<Valve>)

        val path = doDijkstras(
            init = State(0, Valve("AA"), emptySet<Valve>()),
            target = { (t, _, opened) -> t >= limit || opened == openable },
            getNeighbors = { (t, valve, opened) ->
                if (valve !in opened && volcano.valves[valve]!! > 0)
                        setOf(State(t + 1, valve, opened + valve))
                else volcano.tunnels[valve]!!.map { State(t + 1, it, opened) }.toSet()
            },
            getCost = { prev, next ->
                require(prev.time + 1 == next.time)
                if (prev.valve == next.valve)
                    costCeiling - volcano.valves[prev.valve]!! * (limit - prev.time)
                else costCeiling
            }
        )

        val valves = path.map { it.valve }
        assert(valves.isNotEmpty())
        return volcano.pressureReleasedFrom(
            (valves + List(limit - valves.size + 1) { valves.last() })
        )
    }

    override fun part2(input: String): Any {
        val limit = 26
        val volcano = Volcano.fromString(input)
        val costCeiling = volcano.valves.map { limit * it.value }.max()
        val openable = volcano.valves.filter { it.value > 0 }.keys

        data class State(val time: Int, val selfValve: Valve, val elephantValve: Valve, val opened: Set<Valve>) {
            override fun equals(other: Any?): Boolean =
                this === other || when (other) {
                    is State -> (this.selfValve == other.selfValve && this.elephantValve == other.elephantValve || this.selfValve == other.elephantValve && this.elephantValve == other.selfValve) && this.time == other.time && this.opened == other.opened
                    else -> false
                }

            override fun hashCode(): Int {
                var result = time
                result = 31 * result + selfValve.hashCode() * elephantValve.hashCode()
                result = 31 * result + opened.hashCode()
                return result
            }
        }

        operator fun <T, U> Collection<T>.times(other: Collection<U>): Set<Pair<T, U>> =
            this.flatMap { l -> other.map { r -> l to r } }.toSet()

        val path = doDijkstras(
            init = State(0, Valve("AA"), Valve("AA"), emptySet<Valve>()),
            target = { (t, _, _, opened) -> t >= limit || opened == openable },
            getNeighbors = { (t, selfValve, elephantValve, opened) ->
                val nextSelfValves: Set<Valve> by lazy {
                    volcano.tunnels[selfValve]!!.toSet()
                }
                val nextElephantValves: Set<Valve> by lazy {
                    volcano.tunnels[elephantValve]!!.toSet()
                }
                if (selfValve !in opened && volcano.valves[selfValve]!! > 0) {
                    if (elephantValve != selfValve && elephantValve !in opened && volcano.valves[elephantValve]!! > 0) {
                        setOf(State(t + 1, selfValve, elephantValve, opened + selfValve + elephantValve))
                    } else {
                        val opened = opened + selfValve
                        nextElephantValves.map {
                            State(t + 1, selfValve, it, opened)
                        }.toSet()
                    }
                } else {
                    nextSelfValves.flatMap { selfValve -> nextElephantValves.map { elephantValve ->
                        State(t + 1, selfValve, elephantValve, opened)
                    } }.toSet()
                }
            },
            getCost = { prev, next ->
                require(prev.time + 1 == next.time)
                val selfCost = if (prev.selfValve == next.selfValve)
                    costCeiling - volcano.valves[prev.selfValve]!! * (limit - prev.time)
                else costCeiling
                val elephantCost = if (prev.elephantValve == next.elephantValve && (prev.elephantValve != prev.selfValve && prev.elephantValve != next.selfValve))
                    costCeiling - volcano.valves[prev.elephantValve]!! * (limit - prev.time)
                else costCeiling
                selfCost + elephantCost
            }
        )

        val selfValves = path.map { it.selfValve }
        val elephantValves = path.map { it.elephantValve }
        assert(selfValves.isNotEmpty())
        return volcano.pressureReleasedFrom(
            (selfValves + List(limit - selfValves.size + 1) { selfValves.last() }),
            (elephantValves + List(limit - elephantValves.size + 1) { elephantValves.last() })
        )
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
            AA,DD,DD,CC,BB,BB,AA,II,JJ,JJ,II,AA,DD,EE,FF,GG,HH,HH,GG,FF,EE,EE,DD,CC,CC,CC,CC,CC,CC,CC,CC
        """.trimIndent().split(",").map { Valve(it) }

        assertEquals(31, expectedPath.size)
        assertEquals(1651, volcano.pressureReleasedFrom(expectedPath))
//        assertEquals(1651, part1(input))

        val selfPath = """
            AA,II,JJ,JJ,II,AA,BB,BB,CC,CC
        """.trimIndent().split(",").map { Valve(it) }
        val elephantPath = """
            AA,DD,DD,EE,FF,GG,HH,HH,GG,FF,EE,EE
        """.trimIndent().split(",").map { Valve(it) }

        assertEquals(1707, volcano.pressureReleasedFrom(
            (selfPath + List(26 - selfPath.size + 1) { selfPath.last() }),
            (elephantPath + List(26 - elephantPath.size + 1) { elephantPath.last() })
        ))
        assertEquals(1707, part2(input))
    }
}