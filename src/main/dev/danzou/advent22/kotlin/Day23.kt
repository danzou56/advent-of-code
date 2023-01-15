package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.Pos
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day23 : AdventTestRunner22() {

    val ORDER = listOf(
        listOf(Compass.NORTH, Compass.NORTHEAST, Compass.NORTHWEST),
        listOf(Compass.SOUTH, Compass.SOUTHEAST, Compass.SOUTHWEST),
        listOf(Compass.WEST, Compass.NORTHWEST, Compass.SOUTHWEST),
        listOf(Compass.EAST, Compass.NORTHEAST, Compass.SOUTHEAST),
    )
    val DIRECTIONS = ORDER.size

    fun parseInput(input: String): Set<Pos> =
        input.split("\n")
            .flatMapIndexed { y, it -> // y gives the row in the input
                it.mapIndexedNotNull { i, c -> if (c == '#') i else null }
                    .map { x -> Pos(x, y) }
            }.toSet()

    fun run(elves: Set<Pos>, limit: Int = -1): Pair<Set<Pos>, Int> {
        val indexIterator = object : Iterator<Int> {
            var cur = 0
                set(value) {
                    field = value % ORDER.size
                }
            override fun hasNext() = true
            override fun next() = cur++
        }

        tailrec fun step(elves: Set<Pos>, step: Int, limit: Int): Pair<Set<Pos>, Int> {
            if (limit != -1 && step >= limit) return Pair(elves, step + 1)
            // if for all elves, each of their surrounding directions contain nothing, we are done
            if (elves.all { k -> Compass.directions().all { k + it.dir !in elves } })
                return Pair(elves, step + 1)

            val indices = indexIterator.next().let { start -> start until start + DIRECTIONS }
                .map { it % DIRECTIONS }
            return step(elves.map { elf ->
                val availIndex = indices
                        .map { ORDER[it].all { elf + it.dir !in elves } }
                        .let { seq ->
                            if (seq.all { it } || seq.none { it }) return@map Pair(elf, elf)
                            else seq
                        }
                        .indexOfFirst { it } + indices.first()
                val nextPos = elf + ORDER[availIndex % DIRECTIONS].first().dir
                Pair(elf, nextPos)
            }.let {
                val nexts = it.map { it.second }.groupingBy { it }.eachCount()
                it.map { (elf, nextPos) ->
                    if (nexts[nextPos]!! > 1) elf
                    else nextPos
                }.toSet()
            }, step + 1, limit)
        }

        return step(elves, 0, limit)
    }

    override fun part1(input: String): Any {
        val elves = run(parseInput(input), 10).first
        val width = elves.maxOf { it.first } - elves.minOf { it.first } + 1
        val height = elves.maxOf { it.second } - elves.minOf { it.second } + 1
        return (width * height) - elves.size
    }

    override fun part2(input: String): Any =
        run(parseInput(input)).second

    @Test
    fun testSimpleExample() {
        val input = """
            .....
            ..##.
            ..#..
            .....
            ..##.
            .....
        """.trimIndent()

        assertEquals(25, part1(input))
    }

    @Test
    fun testComplexExample() {
        val input = """
            ....#..
            ..###.#
            #...#.#
            .#...##
            #.###..
            ##.#.##
            .#..#..
        """.trimIndent()

        assertEquals(110, part1(input))
        assertEquals(20, part2(input))
    }
}