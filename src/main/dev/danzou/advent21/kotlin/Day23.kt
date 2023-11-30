package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
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

    class Burrow(val width: Int, val height: Int) {
        val ROOMS = (2..height).flatMap { y -> (3..9 step 2).map { Pos(it, y) } }.toSet()
        val ROOM_ENTRANCES = (3..9 step 2).map { Pos(it, 1) }.toSet()
        val HALLWAY = (1..width).map { Pos(it, 1) }.toSet()
        val BURROW = ROOMS + HALLWAY
        val rooms = mapOf(
            Amphipod.Amber to (2..height).map { y -> Pair(3, y) }.toSet(),
            Amphipod.Bronze to (2..height).map { y -> Pair(5, y) }.toSet(),
            Amphipod.Copper to (2..height).map { y -> Pair(7, y) }.toSet(),
            Amphipod.Desert to (2..height).map { y -> Pair(9, y) }.toSet()
        )

        private fun nextBurrows(occupied: SparseMatrix<Amphipod>): Set<Pair<SparseMatrix<Amphipod>, Int>> =
            occupied.keys.map { pos ->
                pos to nextPosForAmphipodAt(occupied, pos)
            }.filter { (_, nexts) ->
                nexts.isNotEmpty()
            }.flatMap { (pos, nexts) ->
                val removed = occupied - pos
                nexts.map { (next, cost) ->
                    Pair(
                        removed + (next to occupied[pos]!!),
                        cost
                    )
                }
            }.toSet()

        private fun nextPosForAmphipodAt(occupied: SparseMatrix<Amphipod>, pos: Pos): Set<Pair<Pos, Int>> {
            require(pos in occupied)
            val amphipod = occupied[pos]!!
            // not allowed to move if already in the right room
            if (pos in rooms[amphipod]!! &&
                (pos.y == height || (pos.y..height).all { y ->
                    occupied[Pos(pos.x, y)] == amphipod
                })
            ) return emptySet()
            return bfs(pos) { cur ->
                // doing this manually instead of using Direction.CARDINAL_DIRECTIONS
                // saves about half a second of runtime
                arrayOf(
                    Pos(cur.x, cur.y - 1),
                    Pos(cur.x, cur.y + 1),
                    Pos(cur.x - 1, cur.y),
                    Pos(cur.x + 1, cur.y),
                )
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
                        target.y == height -> true
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

        fun lowestCostBetween(init: SparseMatrix<Amphipod>, target: SparseMatrix<Amphipod>): Int {
            // costs cache - we get the path length in bfs, and then keep it until we need it
            // otherwise we'd have to recalculate each time
            val costs = mutableMapOf<Pair<SparseMatrix<Amphipod>, SparseMatrix<Amphipod>>, Int>()
            val path = doDijkstras(
                init,
                { it == target },
                { occupied ->
                    val nexts = nextBurrows(occupied)
                    costs.putAll(
                        nexts.map { (next, cost) ->
                            (occupied to next) to cost
                        }
                    )
                    nexts.map { (next, _) -> next }.toSet()
                },
                { src, dst -> costs.remove(src to dst)!! }
            )
            return path.windowed(2).fold(0) { cost, (src, dst) ->
                cost + nextBurrows(src)
                    .find { it.first == dst }!!
                    .second
            }
        }

        companion object {
            fun fromString(input: String): Pair<Burrow, SparseMatrix<Amphipod>> =
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
                }.flatten().toMap().let { occupied ->
                    Pair(
                        Burrow(
                            width = 11,
                            height = occupied.keys.maxOf { (_, y) -> y }
                        ),
                        occupied
                    )
                }
        }
    }

    override fun part1(input: String): Int {
        val (burrow, occupied) = Burrow.fromString(input)
        val (_, target) = Burrow.fromString(
            """
            #############
            #...........#
            ###A#B#C#D###
              #A#B#C#D#
              #########
        """.trimIndent()
        )
        return burrow.lowestCostBetween(occupied, target)
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

        assertEquals(12521, part1(input))
    }

}