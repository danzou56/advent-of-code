package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.sign

internal class Day14 : AdventTestRunner23("Parabolic Reflector Dish") {
    class Platform(matrix: Matrix<Char>) {
        private val height = matrix.size
        private val width = matrix[0].size
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
        // Settable so I can be lazy about implementation and not have to keep on copying all these
        // maps and sets over and over again
        var rounds = _platform.filter { (_, c) -> c == ROUND }.keys
            // The only reason the setter for this is public is so part 2 can easily reuse the load
            // function that's a part of this class
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
            get() = rounds.associateWith { ROUND } +
                    blockers.filter { p -> p.x !in 0..<width && p.y !in 0..<height }
                        .associateWith { CUBE }

        fun tilt(direction: Compass): Platform {
            // Make liberal use of lambdas to make the behavior applicable to all directions.
            // The position on the axis on which the rock can't move
            val posAxis: (Pos) -> Int
            // The position on the axis on which the rock is able to move
            val moveAxis: (Pos) -> Int
            // Given (posAxis, moveAxis), a Pos representing the rock's position on the platform
            // after correcting for not knowing which is x-axis and which is y-axis
            val makePos: (Int, Int) -> Pos
            // The grouped ranges for every column in the posAxis
            val blockers: Map<Int, List<IntRange>>
            when (direction) {
                Compass.NORTH, Compass.SOUTH -> {
                    posAxis = Pos::x
                    moveAxis = Pos::y
                    makePos = ::Pos
                    blockers = blockersGroupedByColumn
                }
                Compass.WEST, Compass.EAST -> {
                    posAxis = Pos::y
                    moveAxis = Pos::x
                    makePos = { a: Int, b: Int -> Pos(b, a) }
                    blockers = blockersGroupedByRow
                }
                else -> throw IllegalArgumentException()
            }
            // The direction the rocks are moving along moveAxis
            val sign = moveAxis(direction.dir).sign
            // The side of the range towards the movement direction
            val rangeStart: (IntRange) -> Int = when (sign) {
                -1 -> { it -> it.first }
                else -> { it -> it.last }
            }

            val groupedRounds = rounds.groupBy(posAxis)
                .mapValues { (_, ps) -> ps.map(moveAxis) }
            // (Assuming NORTH for this explanation) For every column of ranges, count the number of
            // round rocks in each range. Place that many round rocks starting at start of the
            // range, thus "moving" them in the correct direction.
            rounds = blockers
                .mapValues { (index, ranges) ->
                    ranges
                        // Rocks within the same column
                        .map { range -> range to (groupedRounds[index] ?: emptyList()) }
                        // The number of rocks within the given range
                        .map { (range, ps) -> range to ps.count { it in range } }
                        // Repositioned against the start of the range
                        .flatMap { (range, posCount) -> (0..<posCount).map { rangeStart(range) - sign * it } }
                }
                .flatMap { (index, moveds) -> moveds.map { makePos(index, it) } }
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

    override fun part1(input: String): Int {
        val platform = Platform.fromString(input)
        platform.tilt(Compass.NORTH)
        return platform.load()
    }

    override fun part2(input: String): Int {
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

        // Check single move
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

        // Check triple cycle
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