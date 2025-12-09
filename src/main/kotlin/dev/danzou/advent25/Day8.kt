package dev.danzou.advent25

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.Pos3L
import dev.danzou.advent.utils.geometry3.squaredDistanceTo
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent.utils.geometry3.x
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day8 : AdventTestRunner25("Playground") {

  fun getJunctions(input: String): List<Pos3L> =
      input.lines().map { it.split(",").map(String::toLong).toTriple() }

  fun getDistances(junctions: List<Pos3L>): Map<Pair<Pos3L, Pos3L>, Long> {
    val junctionPairs =
        (0..<junctions.size - 1).flatMap { i ->
          val j1 = junctions[i]
          junctions.subList(i + 1, junctions.size).map { j2 -> j1 to j2 }
        }
    val junctionDistances =
        junctionPairs.associateWith { js -> (js.first.squaredDistanceTo(js.second)) }
    return junctionDistances
  }

  fun getComponents(edges: Map<Pos3L, Collection<Pos3L>>): Set<Set<Pos3L>> {
    val components = mutableSetOf<Set<Pos3L>>()

    fun discoverComponent(start: Pos3L): Set<Pos3L> {
      val found = components.firstOrNull { start in it }
      if (found != null) return found

      val discovered = dfs(start) { edges.get(it) ?: emptyList() }
      components.add(discovered)
      return discovered
    }

    edges.keys.onEach { discoverComponent(it) }
    return components
  }

  override fun part1(input: String): Long {
    return part1(input, 1000, 3)
  }

  fun part1(input: String, connect: Int, top: Int): Long {
    val junctions = getJunctions(input)
    val junctionDistances = getDistances(junctions)
    val furthest =
        junctionDistances.entries
            .sortedBy { (_, distance) -> distance }
            .take(connect)
            .map { it.key }
    val edges =
        furthest
            .flatMap { (j1, j2) -> listOf(j1 to j2, j2 to j1) }
            .groupBy { it.first }
            .mapValues { (_, v) -> v.map { it.second } }
    val components = getComponents(edges)

    return components.map { it.size.toLong() }.sorted().takeLast(top).reduce(Long::times)
  }

  override fun part2(input: String): Any {
    val junctions = getJunctions(input)
    val junctionDistances = getDistances(junctions)
    val sortedJunctions =
        junctionDistances.entries.sortedBy { (_, distance) -> distance }.map { it.key }

    val minConnections = mutableSetOf<Pair<Pos3L, Pos3L>>()
    val discovered = mutableSetOf<Pos3L>()

    tailrec fun getMinConnections(cur: Int): Int {
      if (cur >= sortedJunctions.size) throw IllegalArgumentException()
      if (discovered.size == junctions.size) return cur - 1
      val connection = sortedJunctions[cur]
      val (j1, j2) = connection
      if (j1 !in discovered || j2 !in discovered) {
        minConnections.add(connection)
        discovered.add(j1)
        discovered.add(j2)
      }
      return getMinConnections(cur + 1)
    }

    val lastAdded = getMinConnections(0)
    val lastAddedEdge = sortedJunctions[lastAdded]

    return lastAddedEdge.first.x * lastAddedEdge.second.x
  }

  @Test
  fun testExample() {
    """
      162,817,812
      57,618,57
      906,360,560
      592,479,940
      352,342,300
      466,668,158
      542,29,236
      431,825,988
      739,650,466
      52,470,668
      216,146,977
      819,987,18
      117,168,530
      805,96,715
      346,949,466
      970,615,88
      941,993,340
      862,61,35
      984,92,344
      425,690,689
    """
        .trimIndent()
        .let { input ->
          assertEquals(40L, part1(input, 10, 3))
          assertEquals(25272L, part2(input))
        }
  }
}
