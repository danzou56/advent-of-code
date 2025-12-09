package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.get
import java.time.Duration
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private typealias Pose = Pair<Pos, Compass>

internal class Day16 : AdventTestRunner24("Reindeer Maze") {

  val Pose.pos
    get() = this.first

  val Pose.dir
    get() = this.second

  fun calculatePathCost(path: List<Pos>, startCompass: Compass): Int {
    val dirChanges =
        path
            .windowed(2, 1)
            .map { (prev, cur) -> cur - prev }
            .let { dirs -> listOf(startCompass.dir) + dirs }
            .windowed(2, 1)
            .count { (prev, cur) -> prev != cur }
    val pathLength = path.size
    return dirChanges * 1000 + pathLength - 1
  }

  fun getMinPath(maze: Matrix<Char>, start: Pose, end: Pos): List<Pos> =
      doDijkstras(
              init = start,
              target = { (p, _) -> p == end },
              getNeighbors = { (cur, _) ->
                CARDINAL.map { nextDir -> cur + nextDir.dir to nextDir }
                    .filter { (next, _) -> maze.containsPos(next) && maze[next] != '#' }
              },
              getCost = { (prev, prevDir), (next, nextDir) ->
                assert(next.manhattanDistanceTo(prev) == 1)
                if (nextDir != prevDir) 1001 else 1
              })
          .map { it.first }

  override fun part1(input: String): Int {
    val maze = input.asMatrix<Char>()
    val start = maze.indices2D.single { maze[it] == 'S' }
    val end = maze.indices2D.single { maze[it] == 'E' }

    val minPath = getMinPath(maze, start to Compass.EAST, end)
    return calculatePathCost(minPath, Compass.EAST)
  }

  override fun part2(input: String): Int {
    val maze = input.asMatrix<Char>()
    val start = maze.indices2D.single { maze[it] == 'S' }
    val end = maze.indices2D.single { maze[it] == 'E' }

    val minPath = getMinPath(maze, start to Compass.EAST, end)
    val minCost = calculatePathCost(minPath, Compass.EAST)

    fun discoverReachableNodes(init: Pose): Map<Pose, Int> {
      val costs = mutableMapOf(init to 0)
      // vertex is always in cost map so dereference is safe
      val queue = PriorityQueue(Comparator.comparingInt<Pose> { costs[it]!! })
      queue.add(init)

      val predecessors = mutableMapOf<Pose, Pose?>(init to null)

      while (queue.isNotEmpty()) {
        val cur = queue.poll()!!
        val neighbors: List<Pose> =
            CARDINAL.map { nextDir -> cur.pos + nextDir.dir to nextDir }
                .filter { (next, _) -> maze.containsPos(next) && maze[next] != '#' }
                .filter { (next, _) ->
                  assert(cur in predecessors)
                  predecessors[cur]?.pos != next
                }

        for (adjacent in neighbors) {
          val cost = costs[cur]!! + if (cur.dir != adjacent.dir) 1001 else 1

          if (cost < (costs[adjacent] ?: Int.MAX_VALUE) && cost < minCost) {
            costs[adjacent] = cost
            queue.add(adjacent)
            predecessors[adjacent] = cur
          }
        }
      }
      return costs
    }

    val reachablePose = discoverReachableNodes(start to Compass.EAST).toMutableMap()

    val reachablePoseSet =
        minPath
            .windowed(2, 1)
            .map { (prev, cur) -> cur to Compass.fromDir(cur - prev) }
            .let { dirs ->
              if (dirs.first() != start to Compass.EAST)
                  listOf(start to Compass.EAST) + dirs
              else dirs
            }
            .toMutableSet()
    val unreachablePoseSet = mutableSetOf<Pose>()

    fun canReachEnd(init: Pose): Boolean {
      val costs = reachablePose

      val queue = PriorityQueue(Comparator.comparingInt<Pose> { costs[it]!! })
      queue.add(init)

      val predecessors = mutableMapOf<Pose, Pose?>(init to null)

      var cur = queue.poll()!!
      while (cur.pos != end) {
        if (cur in reachablePoseSet) break
        if (cur in unreachablePoseSet)
            return false.also { unreachablePoseSet.addAll(predecessors.keys) }

        val neighbors: List<Pose> =
            CARDINAL.map { nextDir -> cur.pos + nextDir.dir to nextDir }
                .filter { (next, _) -> maze.containsPos(next) && maze[next] != '#' }
                .filter { (next, _) ->
                  assert(cur in predecessors)
                  predecessors[cur]?.pos != next
                }

        for (adjacent in neighbors) {
          val cost = costs[cur]!! + if (cur.dir != adjacent.dir) 1001 else 1

          if (cost <= (costs[adjacent] ?: Int.MAX_VALUE) && cost <= minCost) {
            if (cost != costs[adjacent]) {
//              println("Found faster path! (this shouldn't be possible)")
//              assert(false)
            }
//            assert(cost == costs[adjacent])
            costs[adjacent] = cost
            queue.add(adjacent)
            predecessors[adjacent] = cur
          }
        }

        cur =
            queue.poll()
                ?: return false.also { unreachablePoseSet.addAll(predecessors.keys) }
      }

      while (predecessors[cur] != null) {
        cur = predecessors[cur]!!
        reachablePoseSet.add(cur)
      }

      return true
    }

    reachablePose.keys.toSet().onEach { pose ->
      canReachEnd(pose)
    }
    return reachablePoseSet.map { it.pos }.toSet().size
  }

  @Test
  fun testExample() {
        """
          ###############
          #.......#....E#
          #.#.###.#.###.#
          #.....#.#...#.#
          #.###.#####.#.#
          #.#.#.......#.#
          #.#.#####.###.#
          #...........#.#
          ###.#.#####.#.#
          #...#.....#.#.#
          #.#.#.###.#.#.#
          #.....#...#.#.#
          #.###.#.#.#.#.#
          #S..#.....#...#
          ###############
        """
            .trimIndent()
            .let { input ->
              assertEquals(7036, part1(input))
              assertEquals(45, part2(input))
            }

    """
      #################
      #...#...#...#..E#
      #.#.#.#.#.#.#.#.#
      #.#.#.#...#...#.#
      #.#.#.#.###.#.#.#
      #...#.#.#.....#.#
      #.#.#.#.#.#####.#
      #.#...#.#.#.....#
      #.#.#####.#.###.#
      #.#.#.......#...#
      #.#.###.#####.###
      #.#.#...#.....#.#
      #.#.#.#####.###.#
      #.#.#.........#.#
      #.#.#.#########.#
      #S#.............#
      #################
    """
        .trimIndent()
        .let { input -> assertEquals(64, part2(input)) }
  }
}
