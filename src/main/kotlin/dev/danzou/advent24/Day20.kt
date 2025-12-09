package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus
import java.util.LinkedList
import java.util.Queue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day20 : AdventTestRunner24("Race Condition") {

  companion object {
    val neighborFunction: (Matrix<Char>) -> NeighborFunction<Pos> = { matrix ->
      { pos -> matrix.neighboringPos(pos).filter { matrix[it] != '#' } }
    }
  }

  override fun part1(input: String): Int = part1(input, 100)

  fun part1(input: String, threshold: Int): Int = part2(input, threshold, 2)

  override fun part2(input: String): Int = part2(input, 100, 20)

  fun part2(input: String, threshold: Int, cheat: Int): Int {
    val matrix = input.asMatrix<Char>()
    val start = matrix.indices2D.single { matrix[it] == 'S' }
    val end = matrix.indices2D.single { matrix[it] == 'E' }

    val cleanLength = doDijkstras(start, { it == end }, neighborFunction(matrix)).size
    val distanceToEnd = bfsWithDistance(end, neighborFunction(matrix))

    val cheatOffsets =
        bfsWithDistance(0 to 0) { pos ->
              CARDINAL_DIRECTIONS.map { pos + it }
                  .filter { it.manhattanDistanceTo(0 to 0) <= cheat }
            }
            .filter { (p, _) -> p.manhattanDistanceTo(0 to 0) > 1 }

    var goodCheats = 0

    fun cheatingBfs(
      init: Pos,
      getNeighbors: NeighborFunction<Pos>
    ): Map<Pos, Int> {
      val queue: Queue<Pos> = LinkedList()
      val discovered = mutableMapOf(init to 0)
      queue.add(init)
      while (queue.isNotEmpty()) {
        val cur = queue.poll()!!

        // Check if cheating from cur is good enough
        for ((offset, distance) in cheatOffsets) {
          if (matrix.getOrElse(cur + offset) { '#' } != '#') {
            val cheatLength =
                discovered[cur]!! + distanceToEnd[cur + offset]!! + distance
            if (cheatLength + threshold <= cleanLength) goodCheats += 1
          }
        }

        // Add next adjacents to the queue
        for (adjacent in getNeighbors(cur)) {
          if (adjacent !in discovered) {
            discovered[adjacent] = discovered[cur]!! + 1
            queue.add(adjacent)
          }
        }
      }
      return discovered
    }

    cheatingBfs(start, neighborFunction(matrix))
    return goodCheats
  }

  @Test
  fun testExample() {
    """
      ###############
      #...#...#.....#
      #.#.#.#.#.###.#
      #S#...#.#.#...#
      #######.#.#.###
      #######.#.#...#
      #######.#.###.#
      ###..E#...#...#
      ###.#######.###
      #...###...#...#
      #.#####.#.###.#
      #.#...#.#.#...#
      #.#.#.#.#.#.###
      #...#...#...###
      ###############
    """
        .trimIndent()
        .let { input ->
          assertEquals(1, part1(input, 64))
          assertEquals(5, part1(input, 20))
          assertEquals(16, part1(input, 6))
          assertEquals(7, part2(input, 74, 50))
        }
  }
}
