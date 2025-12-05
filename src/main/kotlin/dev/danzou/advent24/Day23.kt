package dev.danzou.advent24

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day23 : AdventTestRunner24("LAN Party") {

  fun buildGraph(input: String): Map<String, Set<String>> {
    val edges = mutableMapOf<String, MutableSet<String>>()
    input
        .lines()
        .map { it.split("-") }
        .onEach { (v1, v2) ->
          edges.getOrPut(v1) { mutableSetOf() }.add(v2)
          edges.getOrPut(v2) { mutableSetOf() }.add(v1)
        }

    return edges
  }

  override fun part1(input: String): Int {
    val edges = buildGraph(input)
    val tComputer = { edge: String -> edge.startsWith("t") }

    return edges.entries
        .asSequence()
        .flatMap { (e, adjacents) ->
          adjacents
              .pairs()
              .filter { (a1, a2) -> tComputer(e) || tComputer(a1) || tComputer(a2) }
              .filter { (a1, a2) ->
                e in edges[a1]!! &&
                    a2 in edges[a1]!! &&
                    e in edges[a2]!! &&
                    a1 in edges[a2]!!
              }
              .map { (a1, a2) ->
                // faster than creating a set
                listOf(e, a1, a2).sorted().joinToString("")
              }
        }
        .toSet()
        .size
  }

  override fun part2(input: String): String {
    val edges = buildGraph(input)

    val cliques = getCliques(edges)
    return cliques.maxBy { it.size }.sorted().joinToString(",")
  }

  /**
   * Use the Bron-Kerbosch algorithm to list all cliques of the given edge map.
   * This is apparently "fixed-parameter intractable" and "hard to approximate",
   * but I guess the structure of this problem admits the fast computation of a solution
   */
  fun <T> getCliques(graph: Map<T, Set<T>>): Collection<Set<T>> {
    val maximalCliques = mutableListOf<Set<T>>()

    // Implementation of the Bron-Kerbosch algorithm for finding maximal cliques from
    // https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm#With_pivoting
    fun bronKerbosch2(r: Set<T>, p: Set<T>, x: Set<T>) {
      val pivot = p.firstOrNull() ?: x.firstOrNull()
      if (pivot == null) {
        maximalCliques.add(r)
        return
      }

      val p = p.toMutableSet()
      val x = x.toMutableSet()
      for (v in p - graph[pivot]!!) {
        val vNeighbors = graph[v]!!
        bronKerbosch2(r + v, p.intersect(vNeighbors), x.intersect(vNeighbors))
        p.remove(v)
        x.add(v)
      }
    }

    bronKerbosch2(emptySet(), graph.keys, emptySet())
    return maximalCliques
  }

  @Test
  fun testExample() {
    """
      kh-tc
      qp-kh
      de-cg
      ka-co
      yn-aq
      qp-ub
      cg-tb
      vc-aq
      tb-ka
      wh-tc
      yn-cg
      kh-ub
      ta-co
      de-co
      tc-td
      tb-wq
      wh-td
      ta-ka
      td-qp
      aq-cg
      wq-ub
      ub-vc
      de-ta
      wq-aq
      wq-vc
      wh-yn
      ka-de
      kh-ta
      co-tc
      wh-qp
      tb-vc
      td-yn
    """
        .trimIndent()
        .let { input ->
          assertEquals(7, part1(input))
          assertEquals("co,de,ka,ta", part2(input))
        }
  }
}
