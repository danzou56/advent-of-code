package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

internal class Day14 : AdventTestRunner23() {
    val ROUND = 'O'
    val CUBE = '#'
    val EMPTY = '.'

    override fun part1(input: String): Any {
        val (platform, height, width) = getPlatform(input)
        val newRolledNorth = move(platform, height, width, Compass.NORTH)
        return load(newRolledNorth, height)
    }

    fun getPlatform(input: String): Triple<SparseMatrix<Char>, Int, Int> =
        input.asMatrix<Char>().let {
            Triple(
                it.mapIndexed2D { p, c ->
                    p to c
                }.flatten().filter { (_, c) -> c != EMPTY }
                    .toMap(),
                it.size,
                it[0].size,
            )
        }

    fun cycle(platform: SparseMatrix<Char>, height: Int, width: Int): SparseMatrix<Char> =
        platform.let { platform ->
            move(platform, height, width, Compass.NORTH)
        }.let { platform ->
            move(platform, height, width, Compass.WEST)
        }.let { platform ->
            move(platform, height, width, Compass.SOUTH)
        }.let { platform ->
            move(platform, height, width, Compass.EAST)
        }

    fun move(platform: SparseMatrix<Char>, height: Int, width: Int, direction: Compass): SparseMatrix<Char> {

        val (range, posAxis, moveAxis) = when (direction) {
            Compass.NORTH -> Triple(0..<height, (Pos::x), (Pos::y))
            Compass.WEST -> Triple(0..<width, (Pos::y), (Pos::x))
            Compass.SOUTH -> Triple(height - 1 downTo 0, (Pos::x), (Pos::y))
            Compass.EAST -> Triple(width - 1 downTo 0, (Pos::y), (Pos::x))
            else -> throw IllegalArgumentException()
        }

        return range.drop(1).fold(
            platform.filter { (p, _) -> moveAxis(p) == range.first }
        ) { next, index ->
            next + platform.filter { (p, c) -> moveAxis(p) == index && c == ROUND }.mapKeys { (toMove, c) ->
                val minToMoveTo = next.keys
                    .filter { blocker ->
                        posAxis(blocker) == posAxis(toMove) && moveAxis(blocker).compareTo(
                            moveAxis(
                                toMove
                            )
                        ).sign == moveAxis(direction.dir).sign
                    }
                    .map(moveAxis)
                    .reduceOrNull { a, b ->
                        when (moveAxis(direction.dir).sign) {
                            -1 -> max(a, b)
                            else -> min(a, b)
                        }
                    } ?: (range.first() + moveAxis(direction.dir))
                val nextPos = when (direction) {
                    Compass.NORTH -> Pos(toMove.x, minToMoveTo + 1)
                    Compass.WEST -> Pos(minToMoveTo + 1, toMove.y)
                    Compass.SOUTH -> Pos(toMove.x, minToMoveTo - 1)
                    Compass.EAST -> Pos(minToMoveTo - 1, toMove.y)
                    else -> throw IllegalArgumentException()
                }
                nextPos
            } + platform.filter { (p, c) -> moveAxis(p) == index && c == CUBE }
        }.also {
            require(it.size == platform.size)
        }

    }

    fun load(platform: SparseMatrix<Char>, height: Int): Int {
        val rounds = platform.filter { (_, c) -> c == ROUND }.keys
        return rounds.sumOf { (_, y) -> (height - y) }
    }

    override fun part2(input: String): Any {
        val justForSizing = input.asMatrix<Char>()
        var platform: SparseMatrix<Char> = input.asMatrix<Char>().mapIndexed2D { p, c ->
            p to c
        }.flatten().filter { (_, c) -> c != EMPTY }
            .toMap()
        val original = HashMap(platform)

        var i = 0L
        val movements = listOf(Compass.NORTH, Compass.WEST, Compass.SOUTH, Compass.EAST)
        val cycled = mutableMapOf<SparseMatrix<Char>, Long>()
        while (i < 4_000_000_000L) {
            platform = move(platform, justForSizing.size, justForSizing[0].size, movements[(i % 4).toInt()])
            if (true) {
                if (platform !in cycled) cycled.put(platform, i)
                else {
                    throw RuntimeException()
                    require(platform in cycled)
                    val start = cycled[platform]!!
                    val offset = 4_000_000_000L % (i - start)
                    println(cycled.entries.filter { (p, _) -> load(p, justForSizing.size) == 64 }.map { (_, i) -> i })
                    cycled.entries.single { (k, v) -> v >= start && v % i == offset }
                        .let { (p) -> return load(p, justForSizing.size) }
                }
            }
            i++
        }

        return load(platform, justForSizing.size)
    }

    @Test
    fun testExample() {
        val input = """
            O....#....
            O.OO#....#
            .....##...
            OO.#O....O
            .O.....O#.
            O.#..O.#.#
            ..O..#O..O
            .......O..
            #....###..
            #OO..#....
        """.trimIndent()

        // Single move works
        assertEquals(getPlatform(
            """
                OOOO.#.O..
                OO..#....#
                OO..O##..O
                O..#.OO...
                ........#.
                ..#....#.#
                ..O..#.O.O
                ..O.......
                #....###..
                #....#....
            """.trimIndent()
        ).first,
            getPlatform(input).let { (platform, height, width) ->
                move(
                    platform,
                    height,
                    width,
                    Compass.NORTH
                )
            }

        )
        assertEquals(136, part1(input))

        // Triple cycle works
        assertEquals(getPlatform(
            """
                .....#....
                ....#...O#
                .....##...
                ..O#......
                .....OOO#.
                .O#...O#.#
                ....O#...O
                .......OOO
                #...O###.O
                #.OOO#...O
            """.trimIndent()
        ).first,
            getPlatform(input).let { (platform, height, width) ->
                cycle(
                    cycle(
                        cycle(
                            platform,
                            height,
                            width,
                        ), height, width
                    ), height, width
                )
            }

        )

        assertEquals(64, part2(input))
    }
}