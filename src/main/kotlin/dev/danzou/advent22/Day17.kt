package dev.danzou.advent22

import dev.danzou.advent.utils.geometry.Point
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.*
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

class Day17 : AdventTestRunner22() {

    enum class Go(val dir: Direction) {
        LEFT(Direction.LEFT), RIGHT(Direction.RIGHT);
    }

    open class Shape(val points: Set<Point>) {
        constructor(rectangleUnion: RectangleUnion) : this(rectangleUnion.points())

        object Horizontal : Shape(Rectangle(Point(0, 0), Point(3, 0)))
        object Vertical : Shape(Rectangle(Point(0, 0), Point(0, 3)))
        object Plus : Shape(
            RectangleUnion.fromRectangles(
                Rectangle(Point(0, 1), Point(2, 1)),
                Rectangle(Point(1, 0), Point(1, 2)),
            )
        )

        object RightL : Shape(
            RectangleUnion.fromRectangles(
                Rectangle(Point(0, 0), Point(2, 0)),
                Rectangle(Point(2, 0), Point(2, 2)),
            )
        )

        object Square : Shape(Rectangle(Point(0, 0), Point(1, 1)))

        operator fun plus(p: Point): Shape =
            Shape(this.points.map { it + p }.toSet())

        operator fun plus(s: Shape): Shape =
            Shape(this.points + s.points)

        operator fun contains(p: Point): Boolean =
            p in points

        fun move(direction: Direction, considering: Shape): Result<Shape> {
            val movement = this + direction.dir
            return if (canMove(direction, considering)) Result.success(movement)
            else Result.failure(IllegalStateException())
        }

        fun canMove(direction: Direction, considering: Shape): Boolean =
            (this + direction.dir).points.all { p ->
                p.x in 0..6 && p.y >= 0 && p !in considering
            }

    }

    class PieceIterator : Iterator<Shape> {
        private val pieceSequence = arrayOf(
            Shape.Horizontal,
            Shape.Plus,
            Shape.RightL,
            Shape.Vertical,
            Shape.Square
        )
        var cur = 0
            set(value) {
                field = value % pieceSequence.size
            }

        override fun hasNext(): Boolean = true
        override fun next(): Shape = pieceSequence[cur++]
    }

    class DirectionIterator(private val directions: List<Go>) : Iterator<Direction> {
        var cur = 0
            set(value) {
                field = value % directions.size
            }

        override fun hasNext(): Boolean = true
        override fun next(): Direction {
            return directions[cur++].dir
        }
    }

    fun parseInput(input: String): List<Go> =
        input.map {
            when (it) {
                '<' -> Go.LEFT
                '>' -> Go.RIGHT
                else -> throw IllegalArgumentException()
            }
        }

    fun run(limit: Long, gos: List<Go>): Long {
        val pieces = PieceIterator()
        val directions = DirectionIterator(gos)

        val seen = mutableMapOf<Pair<Int, Int>, Pair<Int, Long>>()

        tailrec fun step(stack: Shape, piece: Shape, steps: Long, skip: Boolean): Long {
            require(steps <= limit)
            if (steps == limit) return stack.points.maxOf(Point::y).toLong() + 1

            val movedLeftRight = piece.move(directions.next(), stack)
                .getOrDefault(piece)
            val movedDownResult = movedLeftRight.move(Direction.DOWN, stack)
            val movedDown = movedDownResult
                .getOrDefault(movedLeftRight)

            if (movedDownResult.isSuccess) return step(stack, movedDown, steps, skip)

            val next = (stack + movedDown).let { stack ->
                val newPoints = movedDown.points.map(Point::y).toSet()
                val lines = stack.points.filter { it.y in newPoints }
                    .groupBy(Point::y)
                    .filter { (_, ps) -> ps.map(Point::x).size == 7 }
                // Can't cull anything
                if (lines.isEmpty()) return@let stack

                // Cull everything below
                val min = lines.keys.min()
                val filtered = Shape(stack.points.filter { (_, y) -> y >= min }.toSet())
                // Don't perform cycle logic when we're doing the final leg
                if (!skip) return@let filtered

                // Store height and steps in cache accessed by piece and direction cursor (and
                // implicit stack shape)
                val height = stack.points.maxOf(Point::y)
                val key = pieces.cur to directions.cur
                if (key !in seen) {
                    seen[key] = height to steps
                    return@let filtered
                }

                // If we've been here before, calculate the offsets so we can skip to the end
                val (lastSeenHeight, lastSeenSteps) = seen[key]!!
                val heightDiff = height - lastSeenHeight
                val stepsDiff = steps - lastSeenSteps
                val remainingCycles = (limit - steps) / stepsDiff
                val offset = (limit - steps) % stepsDiff
                require(steps + stepsDiff * remainingCycles + offset == limit)
                // Apparently you're allowed to mix tailrec and non-tailrec calls - tailrec calls
                // will be optimized and non-tailrec calls won't be!
                return heightDiff * remainingCycles + step(
                    stack,
                    pieces.next() + Pos(2, height + 3 + 1),
                    limit - offset + 1,
                    false
                )
            }
            return step(
                next,
                pieces.next() + Pos(2, next.points.maxOf { it.y } + 3 + 1),
                steps + 1,
                skip
            )
        }

        return step(Shape(emptySet()), pieces.next() + Pos(2, 3), 0, true)
    }

    override fun part1(input: String): Any {
        val gos = parseInput(input)
        return run(2022, gos)
    }

    override fun part2(input: String): Any {
        val gos = parseInput(input)
        return run(1000000000000L, gos)
    }

    @Test
    fun testExample() {
        val input = """
            >>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>
        """.trimIndent()

        assertEquals(17L, run(10, parseInput(input)))
        assertEquals(3068L, part1(input))
        // For some reason, the cycle detecting logic breaks the example input
        assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            assertEquals(1514285714288L, part2(input))
        }
    }

}