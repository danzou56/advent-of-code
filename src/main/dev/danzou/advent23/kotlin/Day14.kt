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
    class Platform(matrix: Matrix<Char>) {
        val height = matrix.size
        val width = matrix[0].size
        private val _platform = matrix
            .mapIndexed2D { p, c -> p to c }
            .flatten()
            .filter { (_, c) -> c != EMPTY }
            .toMap()
            .plus(
                ((0..<width).flatMap { x -> listOf(x to -1, x to height) } +
                        (0..<height).flatMap { y -> listOf(-1 to y, width to y) })
                    .associateWith { CUBE }
            )
        private var rounds = _platform.filter { (_, c) -> c == ROUND }.keys
        private val blockers = _platform.filter { (_, c) -> c == CUBE }.keys
        private val blockersGroupedByColumn = blockers.groupBy(Pos::x)
            .mapValues { (_, ps) ->
                ps.map(Pos::y)
                    .sorted()
                    .windowed(2)
                    .map { (f, l) -> (f + 1)..<l }
                    .filter { !it.isEmpty() }
            }
        private val blockersGroupedByRow = blockers.groupBy(Pos::y)
            .mapValues { (_, ps) ->
                ps.map(Pos::x)
                    .sorted()
                    .windowed(2)
                    .map { (f, l) -> (f + 1)..<l }
                    .filter { !it.isEmpty() }
            }
        val platform
            get() = rounds.associateWith { ROUND } + blockers.filter { p -> p.x !in 0..<width && p.y !in 0..<height }
                .associateWith { CUBE }

        fun tilt(direction: Compass): Platform {
            /*
                        val (pairBuilder: (Int, Int) -> Pair<Int, Int>, posAxis, moveAxis) = when (direction) {
                            Compass.NORTH -> Triple({ posAxis: Int, moveAxis: Int -> Pair(posAxis, moveAxis) }, (Pos::x), (Pos::y))
                            Compass.WEST -> Triple({ posAxis: Int, moveAxis: Int -> Pair(moveAxis, posAxis) }, (Pos::y), (Pos::x))
                            Compass.SOUTH -> Triple({ posAxis: Int, moveAxis: Int -> Pair(posAxis, moveAxis) }, (Pos::x), (Pos::y))
                            Compass.EAST -> Triple({ posAxis: Int, moveAxis: Int -> Pair(moveAxis, posAxis) }, (Pos::y), (Pos::x))
                            else -> throw IllegalArgumentException()
                        }
                        val blockers = when (direction) {
                            Compass.NORTH -> blockersGroupedByColumn
                            Compass.SOUTH -> blockersGroupedByColumn
                            Compass.EAST -> blockersGroupedByRow
                            Compass.WEST -> blockersGroupedByRow
                            else -> throw IllegalArgumentException()
                        }
            */

            val pairBuilder: (Int, Int) -> Pair<Int, Int>
            val posAxis: (Pos) -> Int
            val moveAxis: (Pos) -> Int
            val blockers: Map<Int, List<IntRange>>
            when (direction) {
                Compass.NORTH, Compass.SOUTH -> {
                    pairBuilder = ::Pair
                    posAxis = Pos::x
                    moveAxis = Pos::y
                    blockers = blockersGroupedByColumn
                }

                Compass.WEST, Compass.EAST -> {
                    pairBuilder = { a: Int, b: Int -> Pair(b, a) }
                    posAxis = Pos::y
                    moveAxis = Pos::x
                    blockers = blockersGroupedByRow
                }

                else -> throw IllegalArgumentException()
            }
            val sign = moveAxis(direction.dir).sign
            val rangeEnd: (IntRange) -> Int = when (sign) {
                -1 -> { it: IntRange -> it.first }
                else -> { it: IntRange -> it.last }
            }

            val groupedRounds = rounds.groupBy(posAxis)
                .mapValues { (_, ps) -> ps.map(moveAxis) }
            rounds = blockers
                .mapValues { (index, ps) ->
                    ps.map { range -> range to (groupedRounds[index] ?: emptyList()) }
                        .map { (range, list) -> range to list.count { it in range } }
                        .map { (range, count) -> range to (0..<count).map { rangeEnd(range) - sign * it } }
                        .flatMap { (_, list) -> list }
                }
                .flatMap { (pos, list) -> list.map { pairBuilder(pos, it) } }
                .also { require(it.size == it.toSet().size) }
                .toSet()
                .also { require(it.size == rounds.size) }
            return this
        }

        fun cycle(): Platform {
            tilt(Compass.NORTH)
            tilt(Compass.WEST)
            tilt(Compass.SOUTH)
            tilt(Compass.EAST)
            return this
        }

        fun load() = rounds.sumOf { (_, y) -> (height - y) }

        companion object {
            val ROUND = 'O'
            val CUBE = '#'
            val EMPTY = '.'

            fun fromString(input: String): Platform = Platform(input.asMatrix<Char>())
        }
    }

    override fun part1(input: String): Any {
        val platform = Platform.fromString(input)
        platform.tilt(Compass.NORTH)
        return platform.load()
    }

    fun load(platform: SparseMatrix<Char>, height: Int): Int {
        val rounds = platform.filter { (_, c) -> c == Platform.ROUND }.keys
        return rounds.sumOf { (_, y) -> (height - y) }
    }

    override fun part2(input: String): Any {
        var platform = Platform.fromString(input)
        val cycles = 1_000_000_000L
        val cycled = mutableMapOf<SparseMatrix<Char>, Long>()
        var cur = 0L
        while (cur < cycles) {
            platform.cycle()
            if (platform.platform !in cycled) cycled[platform.platform] = cur
            else {
                require(platform.platform in cycled)
                val start = cycled[platform.platform]!!
                val cycleLength = cur - start
                val offset = (1_000_000_000L - start) % cycleLength
//                println(cycled.entries.filter { (p, _) -> load(p, height) == 64 }.map { (_, i) -> i })
                // Why is minus 1 required here???? Where is off by one coming from?
                cycled.entries.single { (_, i) -> i >= start && (i - start) % cycleLength == offset - 1 }
                    .let { (p, _) -> return load(p, platform.height) }
            }
//            cycled[platform] = cur
//            if (cur > 50)
//                throw RuntimeException()
            cur++
        }
        return 0
//        return load(platform, height)
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
        assertEquals(
            Platform.fromString(
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
            ).platform,
            Platform.fromString(input).tilt(Compass.NORTH).platform
        )
        assertEquals(136, part1(input))

        // Triple cycle works
        assertEquals(
            Platform.fromString(
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
            ).platform,
            Platform.fromString(input).cycle().cycle().cycle().platform
        )

        assertEquals(64, part2(input))
    }
}