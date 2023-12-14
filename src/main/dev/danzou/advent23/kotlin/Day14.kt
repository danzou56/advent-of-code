package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        val sign = moveAxis(direction.dir).sign
        val extremeFunction: (Int, Int) -> Int = when (sign) {
            -1 -> ::max
            1 -> ::min
            else -> throw IllegalArgumentException()
        }

        return range.drop(1).fold(
            platform.filter { (p, _) -> moveAxis(p) == range.first }
        ) { next, index ->
            next + platform.filter { (p, c) -> c == ROUND && moveAxis(p) == index  }.mapKeys { (toMove, _) ->
                val extreme = next.keys
                    .filter { blocker -> posAxis(blocker) == posAxis(toMove) }
                    .filter { blocker -> moveAxis(blocker).compareTo(moveAxis(toMove)).sign == sign}
                    .map(moveAxis)
                    .reduceOrNull(extremeFunction) ?: (range.first() + sign)
                val nextPos = when (direction) {
                    Compass.NORTH -> Pos(toMove.x, extreme - sign)
                    Compass.WEST -> Pos(extreme - sign, toMove.y)
                    Compass.SOUTH -> Pos(toMove.x, extreme - sign)
                    Compass.EAST -> Pos(extreme - sign, toMove.y)
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
        var (platform, _, _) = getPlatform(input)
        val (_, height, width) = getPlatform(input)
        val cycles = 1_000_000_000L
        val cycled = mutableMapOf<SparseMatrix<Char>, Long>()
        var cur = 0L
        while (cur < cycles) {
            platform = cycle(platform, height, width)
            if (platform !in cycled) cycled[platform] = cur
            else {
                require(platform in cycled)
                val start = cycled[platform]!!
                val cycleLength = cur - start
                val offset = (1_000_000_000L - start) % cycleLength
//                println(cycled.entries.filter { (p, _) -> load(p, height) == 64 }.map { (_, i) -> i })
                // Why is minus 1 required here???? Where is off by one coming from?
                cycled.entries.single { (_, i) -> i >= start && (i - start) % cycleLength == offset - 1 }
                    .let { (p, _) -> return load(p, height) }
            }
//            cycled[platform] = cur
//            if (cur > 50)
//                throw RuntimeException()
            cur++
        }

        return load(platform, height)
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