package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

internal class Day25 : AdventTestRunner23() {

    override val timeout = Duration.ofMinutes(1)

    override fun part1(input: String): Any {
        val edges: Map<String, Set<String>> = mutableMapOf<String, Set<String>>().apply {
            input.split("\n").map {
                it.split(": ")
            }.map { (component, components) ->
                components.split(" ").onEach { c ->
                    this[c] = (this[c] ?: emptySet()) + component
                }.let { cs ->
                    this[component] = (this[component] ?: emptySet()) + cs
                }
            }
        }

        val edgeSet = edges.entries
            .flatMap { (key, values) ->
                values.map {
                    Pair(minOf(key, it), maxOf(key, it))
                }
            }
            .toSet()

        // Seed of 42 happens to produce an answer "quickly" - setting the seed is mostly to help
        // with making the development-test loop quicker
        val random = Random(42)

        while (true) {
            val superEdges = LinkedList(edgeSet)
            // We want there to be at least two unique elements, but it's expensive to turn the
            // list into a set. Instead, we can check that there are at least two unique elements
            // by finding the first element that is neither the first nor the last element (in the
            // case where first == last, then iterate through the list)
            while (
                superEdges.run {
                    this.firstOrNull { it != first && it != last } != null
                }
            ) contract(superEdges, superEdges[random.nextInt(superEdges.indices)])

            val extractedSuperEdge = try {
                superEdges.toSet()
                    .flatMap {
                        it.toList().map { it.windowed(3, step = 3).toSet() }
                    }.toPair()
            } catch (e: IllegalArgumentException) {
                continue
            }

            if (
                edges.filter { (vertex, _) ->
                    vertex in extractedSuperEdge.first
                }.mapValues { (_, adjacents) ->
                    adjacents.intersect(extractedSuperEdge.second)
                }.entries.sumOf { (_, adjacents) -> adjacents.size } == 3
            )
                return extractedSuperEdge.first.size * extractedSuperEdge.second.size
        }
    }

    fun contract(edges: LinkedList<Pair<String, String>>, remove: Pair<String, String>) {
        val first = remove.first
        val last = remove.second
        val contracted = "$first$last"

        val iter = edges.listIterator()
        while (iter.hasNext()) {
            val el = iter.next()
            if (el.first == remove.first && el.second == remove.second) {
                iter.remove()
                continue
            }

            val isFirst = first == el.first || last == el.first
            val isSecond = first == el.second || last == el.second
            if (isFirst && isSecond) {
                iter.remove()
                continue
            }
            if (isFirst || isSecond) {
                val other = if (isFirst) {
                    el.second
                } else {
                    el.first
                }
                iter.set(
                    Pair(
                        minOf(contracted, other),
                        maxOf(contracted, other)
                    )
                )
            }
        }
    }

    override fun part2(input: String): String = "Congratulations!"

    @Test
    fun testExample() {
        val input = """
            jqt: rhn xhk nvd
            rsh: frs pzl lsr
            xhk: hfx
            cmg: qnr nvd lhk bvb
            rhn: xhk bvb hfx
            bvb: xhk hfx
            pzl: lsr hfx nvd
            qnr: nvd
            ntq: jqt hfx bvb xhk
            nvd: lhk
            lsr: lhk
            rzs: qnr cmg lsr rsh
            frs: qnr lhk lsr
        """.trimIndent()

        assertEquals(54, part1(input))
    }
}