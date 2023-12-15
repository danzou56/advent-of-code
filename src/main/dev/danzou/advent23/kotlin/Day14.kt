package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        // The only reason the setter for this is public is so part 2 can easily reuse the load
        // function that's a part of this class
        var rounds = _platform.filter { (_, c) -> c == ROUND }.keys
            set(rounds) {
                require(this.rounds.size == rounds.size)
                field = rounds
            }
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
                -1 -> { it -> it.first }
                else -> { it -> it.last }
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
                .toSet()
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
            const val ROUND = 'O'
            const val CUBE = '#'
            const val EMPTY = '.'

            fun fromString(input: String): Platform = Platform(input.asMatrix<Char>())
        }
    }

    override fun part1(input: String): Any {
        val platform = Platform.fromString(input)
        platform.tilt(Compass.NORTH)
        return platform.load()
    }

    override fun part2(input: String): Any {
        val platform = Platform.fromString(input)
        val cycled = mutableMapOf<Set<Pos>, Long>()
        val max = 1_000_000_000L

        // Start at 1 as the original platform is "0"; changing where we start affects the offset
        // of `max` (calculated `(max - start) % interval`)
        val stop = generateSequence(1L) { it + 1 }.first { cur ->
            platform.cycle()
            cycled.putIfAbsent(platform.rounds, cur) != null
        }

        val start = cycled[platform.rounds]!!
        val interval = stop - start
        val offset = (max - start) % interval
        return cycled.entries
            .single { (_, i) -> i >= start && (i - start) % interval == offset }
            .let { (rounds, _) ->
                platform.rounds = rounds
                platform.load()
            }
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