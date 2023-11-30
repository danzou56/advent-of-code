package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class Day23 : AdventTestRunner21("Amphipod") {
    sealed class Amphipod(val cost: Int) {
        data object Amber : Amphipod(1)
        data object Bronze : Amphipod(10)
        data object Copper : Amphipod(100)
        data object Desert : Amphipod(1000)
    }

    data class Burrow(val occupied: Map<Pos, Amphipod>) {
        fun nextBurrows(): Set<Pair<Burrow, Int>> =
            occupied.keys.map { pos ->
                pos to nextPosForAmphipodAt(pos)
            }.filter { (_, nexts) ->
                nexts.isNotEmpty()
            }.flatMap { (pos, nexts) ->
                val removed = occupied - pos
                nexts.filter { (next, cost) ->
                    cost > 0
                }.map { (next, cost) ->
                    Pair(
                        Burrow(removed + (next to occupied[pos]!!)),
                        cost
                    )
                }
            }.toSet()

        fun nextPosForAmphipodAt(pos: Pos): Set<Pair<Pos, Int>> {
            require(pos in occupied)
            val amphipod = occupied[pos]!!
            // not allowed to move if already in the right room
            if (pos in rooms[amphipod]!! && pos.y == 3) return emptySet()
            if (pos in rooms[amphipod]!! && occupied[pos + Pos(0, 1)] == amphipod) return emptySet()
            return bfs(pos) { cur ->
                CARDINAL_DIRECTIONS.map { cur + it }
                    .filter { it in BURROW }
                    .filter { it !in occupied.keys }
                    .toSet()
            }.filter { path ->
                when (val target = path.last()) {
                    // room entrances are not allowed
                    in ROOM_ENTRANCES -> false
                    // hallway is not allowed if we started in hallway
                    in HALLWAY -> pos !in HALLWAY
                    // only entering correct room is allowed
                    in rooms[amphipod]!! -> when {
                        // enter the bottom cell
                        target.y == 3 -> true
                        // next cell down must be filled by the correct amphipod
                        occupied[target + Pos(0, 1)] == amphipod -> true
                        else -> false
                    }
                    // implies we were in a ROOM, but not the right one
                    else -> false
                }
            }.map { path ->
                path.last() to (path.size - 1) * amphipod.cost
            }.toSet()
        }

        companion object {
            val width = 11
            val height = 3
            val rooms = mapOf(
                Amphipod.Amber to setOf(Pair(3, 2), Pair(3, 3)),
                Amphipod.Bronze to setOf(Pair(5, 2), Pair(5, 3)),
                Amphipod.Copper to setOf(Pair(7, 2), Pair(7, 3)),
                Amphipod.Desert to setOf(Pair(9, 2), Pair(9, 3))
            )

            val ROOMS = (3..9 step 2).map { Pos(it, 2) }.toSet() +
                    (3..9 step 2).map { Pos(it, 3) }.toSet()
            val ROOM_ENTRANCES = (3..9 step 2).map { Pos(it, 1) }.toSet()
            val HALLWAY = (1..width).map { Pos(it, 1) }.toSet()
            val BURROW = ROOMS + HALLWAY

            fun isInCorrectRoom(amphipod: Amphipod, pos: Pos): Boolean {
                require(pos in ROOMS)
                return pos.x == when (amphipod) {
                    Amphipod.Amber -> 3
                    Amphipod.Bronze -> 5
                    Amphipod.Copper -> 7
                    Amphipod.Desert -> 9
                }
            }

            fun fromString(input: String): Burrow =
                input.split("\n").mapIndexedNotNull { y, l ->
                    l.mapIndexedNotNull { x, c ->
                        when (c) {
                            'A' -> Pos(x, y) to Amphipod.Amber
                            'B' -> Pos(x, y) to Amphipod.Bronze
                            'C' -> Pos(x, y) to Amphipod.Copper
                            'D' -> Pos(x, y) to Amphipod.Desert
                            else -> null
                        }
                    }.takeIf(List<Pair<Pos, Amphipod>>::isNotEmpty)
                }.flatten().toMap().let(::Burrow)
        }
    }

    fun getLowestCostTo(init: Burrow, target: Burrow): Int {
        val costs = mutableMapOf<Pair<Burrow, Burrow>, Int>()
        val path = doDijkstras(
            init,
            { it == target },
            { burrow ->
                val nexts = burrow.nextBurrows()
                costs.putAll(
                    nexts.map { (next, cost) ->
                        (burrow to next) to cost
                    }
                )
                nexts.map { (next, _) -> next }.toSet()
            },
            { src, dst -> costs.remove(src to dst)!! }
        )
        return path.windowed(2).fold(0) { cost, (src, dst) ->
            cost + src.nextBurrows()
                .find { it.first == dst }!!
                .second
        }
    }

    override fun part1(input: String): Int {
        val burrow = Burrow.fromString(input)
        return getLowestCostTo(burrow, TARGET_BURROW)
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
            #############
            #...........#
            ###B#C#B#D###
              #A#D#C#A#
              #########
        """.trimIndent()

        assertEquals(40, getLowestCostTo(
            Burrow.fromString(input),
            Burrow.fromString("""
                #############
                #...B.......#
                ###B#C#.#D###
                  #A#D#C#A#
                  #########
            """.trimIndent())
        ))
        assertEquals(400, getLowestCostTo(
            Burrow.fromString("""
                #############
                #...B.......#
                ###B#C#.#D###
                  #A#D#C#A#
                  #########
            """.trimIndent()),
            Burrow.fromString("""
                #############
                #...B.......#
                ###B#.#C#D###
                  #A#D#C#A#
                  #########
            """.trimIndent())
        ))
        assertEquals(3030, getLowestCostTo(
            Burrow.fromString("""
                #############
                #...B.......#
                ###B#.#C#D###
                  #A#D#C#A#
                  #########
            """.trimIndent()),
            Burrow.fromString("""
                #############
                #.....D.....#
                ###B#.#C#D###
                  #A#B#C#A#
                  #########
            """.trimIndent())
        ))
        assertEquals(40, getLowestCostTo(
            Burrow.fromString("""
                #############
                #.....D.....#
                ###B#.#C#D###
                  #A#B#C#A#
                  #########
            """.trimIndent()),
            Burrow.fromString("""
                #############
                #.....D.....#
                ###.#B#C#D###
                  #A#B#C#A#
                  #########
            """.trimIndent())
        ))
        assertEquals(2003, getLowestCostTo(
            Burrow.fromString("""
                #############
                #.....D.....#
                ###.#B#C#D###
                  #A#B#C#A#
                  #########
            """.trimIndent()),
            Burrow.fromString("""
                #############
                #.....D.D.A.#
                ###.#B#C#.###
                  #A#B#C#.#
                  #########
            """.trimIndent())
        ))
        assertEquals(7000, getLowestCostTo(
            Burrow.fromString("""
                #############
                #.....D.D.A.#
                ###.#B#C#.###
                  #A#B#C#.#
                  #########
            """.trimIndent()),
            Burrow.fromString("""
                #############
                #.........A.#
                ###.#B#C#D###
                  #A#B#C#D#
                  #########
            """.trimIndent())
        ))
        assertEquals(8, getLowestCostTo(
            Burrow.fromString("""
                #############
                #.........A.#
                ###.#B#C#D###
                  #A#B#C#D#
                  #########
            """.trimIndent()),
            TARGET_BURROW
        ))
        assertEquals(12521, part1(input))
    }

    companion object {
        val TARGET_BURROW = Burrow.fromString("""
            #############
            #...........#
            ###A#B#C#D###
              #A#B#C#D#
              #########
        """.trimIndent())
    }
}