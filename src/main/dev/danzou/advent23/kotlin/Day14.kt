package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Direction
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
        val platform = input.asMatrix<Char>()

        fun moveNorth(matrix: Matrix<Char>, row: Int): Matrix<Char> {
            if (row == 0) return matrix
            else {
                val above = matrix[row - 1].mapIndexed { col, c ->
                    if (c == EMPTY && matrix[row][col] == ROUND) ROUND
                    else c
                }
                val cur = matrix[row].mapIndexed { col, c ->
                    if (c == ROUND && matrix[row - 1][col] == EMPTY) EMPTY
                    else c
                }
                return matrix.mapIndexed { i, list ->
                    if (i == row - 1) above
                    else if (i == row) cur
                    else list
                }
            }
        }

        val rolledNorth = (1..<platform.size).fold(platform) { platform, index ->
            (1..<platform.size).fold(platform) { platform, index ->
                moveNorth(platform, index)
            }
        }.mapIndexed2D { p, c ->
            p to c
        }.flatten().filter { (_, c) -> c != EMPTY }
            .toMap()

        val trueRes = load(rolledNorth, platform.size)

        val newRolledNorth = move(input.asMatrix<Char>().mapIndexed2D { p, c ->
            p to c
        }.flatten().filter { (_, c) -> c != EMPTY }
            .toMap(), platform.size, platform[0].size, Compass.NORTH)

//        return newRes

        return load(newRolledNorth, platform.size)
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

    fun load(platform: Matrix<Char>): Int {
        return platform
            .reversed()
            .mapIndexed { i, row ->
                (i + 1) * row.count { it == ROUND }
            }.sum()
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
                    println(cycled.entries.filter { (p, _) -> load(p, justForSizing.size) == 64 }.map { (_, i) -> i})
                    cycled.entries.single { (k, v) -> v >= start && v % i == offset }
                        .let { (p) -> return load(p, justForSizing.size) }
                }
            }
//            if (platform == cycled) println("woah")
//            if (i % 1_000_000L == 0L) println("LOL")
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

        assertEquals(136, part1(input))
        assertEquals(64, part2(input))
    }
}