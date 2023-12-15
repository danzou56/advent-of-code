package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.Graph
import dev.danzou.advent.utils.Vertex
import dev.danzou.advent.utils.findPaths
import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner21() {
    private fun getGraph(input: String): Graph<String> =
        input.split("\n")
            .map { line -> line.split("-") }
            .map { listOf(Vertex(it[0]), Vertex(it[1])) }
            .let { edgeList ->
                Graph(
                    verticies = edgeList.flatten().toSet(),
                    edges = edgeList.map(List<Vertex<String>>::toPair)
                        .fold(emptyMap()) { map, (v1, v2) ->
                            map + Pair(v1, (map[v1] ?: emptySet()) + v2) + Pair(v2, (map[v2] ?: emptySet()) + v1)
                        }
                )
            }

    override fun part1(input: String): Any {
        val graph = getGraph(input)
        val paths = findPaths(
            init = Vertex("start"),
            target = Vertex("end"),
        ) { vertex, path ->
            graph.getNeighbors(vertex).filter { neighbor ->
                when {
                    neighbor.name.all(Char::isUpperCase) -> true
                    neighbor !in path -> true
                    else -> false
                }
//                neighbor !in path.filter { it.name.all(Char::isLowerCase) }
//                    .filter { path.count { it.name == neighbor.name } > 1 || it.name in listOf("start", "end") }
            }.toSet()
        }
        return paths.size
    }

    override fun part2(input: String): Any {
        val graph = getGraph(input)
        val paths = findPaths(
            init = Vertex("start"),
            target = Vertex("end"),
        ) { vertex, path ->
            graph.getNeighbors(vertex).filter { neighbor ->
                when {
                    neighbor.name.all(Char::isUpperCase) -> true
                    neighbor.name == "start" -> false
                    neighbor !in path -> true
                    path.count { it == neighbor } >= 2 -> false
                    path.filter { it.name.all(Char::isLowerCase) }
                        .groupingBy { it }
                        .eachCount()
                        .any { it.value >= 2 } -> false
                    else -> true
                }
            }.toSet()
        }

        println(paths.filter { path ->
            path.filter { it.name.all(Char::isLowerCase) }
                .groupingBy { it }
                .eachCount()
                .count { it.value >= 2 } > 1
        }.size)
        // Absolutely awful solution to remove incorrect solutions
        // Completely unclear why we're generating more solutions
        return paths.size - paths.filter { path ->
            path.filter { it.name.all(Char::isLowerCase) }
                .groupingBy { it }
                .eachCount()
                .values
                .count { it >= 2 } > 1
        }.size
    }

    @Test
    fun testExample() {
        val input = """
            start-A
            start-b
            A-c
            A-b
            b-d
            A-end
            b-end
        """.trimIndent()

        assertEquals(10, part1(input))
        assertEquals(36, part2(input))
    }

    @Test
    fun testLargeExample() {
        val input = """
            dc-end
            HN-start
            start-kj
            dc-start
            dc-HN
            LN-dc
            HN-end
            kj-sa
            kj-HN
            kj-dc
        """.trimIndent()

        assertEquals(19, part1(input))
        assertEquals(103, part2(input))
    }

    @Test
    fun testLargerExample() {
        val input = """
            fs-end
            he-DX
            fs-he
            start-DX
            pj-DX
            end-zg
            zg-sl
            zg-pj
            pj-he
            RW-he
            fs-DX
            pj-RW
            zg-RW
            start-pj
            he-WI
            zg-he
            pj-fs
            start-RW
        """.trimIndent()

        assertEquals(226, part1(input))
        assertEquals(3509, part2(input))
    }

}