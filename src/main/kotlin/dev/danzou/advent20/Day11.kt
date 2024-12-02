package dev.danzou.advent20

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Compass.Companion.ALL
import dev.danzou.advent.utils.geometry.Compass.Companion.ALL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner20("") {
  val CHAIR = 'L'
  val FLOOR = '.'
  val OCCUPIED = '#'

  override fun part1(input: String): Any {
    val room = input.asMatrix<Char>()

    tailrec fun step(room: Matrix<Char>): Matrix<Char> {
      val next =
          room.mapIndexed2D { p, state ->
            val adjacentOccupied =
                room.neighboringPos(p, ALL_DIRECTIONS).count { adj ->
                  room[adj] == OCCUPIED
                }
            when {
              state == CHAIR && adjacentOccupied == 0 -> OCCUPIED
              state == OCCUPIED && adjacentOccupied >= 4 -> CHAIR
              else -> state
            }
          }
      if (next == room) return room
      return step(next)
    }

    return step(room).flatten().count { it == OCCUPIED }
  }

  override fun part2(input: String): Any {
    val room = input.asMatrix<Char>()
    val roomIndices = room.indices2D.toSet()

    tailrec fun expand(cur: List<Pair<Pos, Compass>>): List<Pos> {
      val next =
          cur.filter { (pos, _) -> pos in roomIndices }
              .map { (pos, compass) ->
                if (room[pos] == CHAIR) pos to compass else (pos + compass.dir) to compass
              }
      return if (cur == next) next.map { (pos, _) -> pos } else expand(next)
    }

    val projectedNeighbors =
        roomIndices.associateWith { p ->
          expand(ALL.map { compass -> p + compass.dir to compass })
        }

    tailrec fun step(room: Matrix<Char>): Matrix<Char> {
      val next =
          room.mapIndexed2D { p, state ->
            when (state) {
              FLOOR -> FLOOR
              CHAIR ->
                  if (projectedNeighbors[p]!!.find { room[it] == OCCUPIED } == null)
                      OCCUPIED
                  else CHAIR
              OCCUPIED ->
                  if (projectedNeighbors[p]!!.count { room[it] == OCCUPIED } >= 5) CHAIR
                  else OCCUPIED
              else -> throw IllegalArgumentException()
            }
          }
      if (next == room) return room
      return step(next)
    }

    return step(room).flatten().count { it == OCCUPIED }
  }

  @Test
  fun testExample() {
    val input =
        """
          L.LL.LL.LL
          LLLLLLL.LL
          L.L.L..L..
          LLLL.LL.LL
          L.LL.LL.LL
          L.LLLLL.LL
          ..L.L.....
          LLLLLLLLLL
          L.LLLLLL.L
          L.LLLLL.LL
        """
            .trimIndent()

    assertEquals(37, part1(input))
    assertEquals(26, part2(input))
  }
}
