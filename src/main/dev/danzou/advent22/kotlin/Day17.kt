package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.Point
import dev.danzou.advent.utils.Pos
import dev.danzou.advent.utils.geometry.*
import dev.danzou.advent.utils.x
import dev.danzou.advent.utils.y
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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

    class PieceIterator(private val limit: Int) : Iterator<Shape> {
         private val pieceSequence = arrayOf(
            Shape.Horizontal,
            Shape.Plus,
            Shape.RightL,
            Shape.Vertical,
            Shape.Square
        )
        private var cur = 0

        override fun hasNext(): Boolean = cur <= limit
        override fun next(): Shape = pieceSequence[cur++ % pieceSequence.size]
    }

    class DirectionIterator(private val directions: List<Go>) : Iterator<Direction> {
        private var cur = 0
            set(value) {
                field = value % directions.size
            }

        override fun hasNext(): Boolean = true
        override fun next(): Direction {
            return directions[cur++].dir
        }
    }

    fun parseInput(input: String): List<Go> =
        input.map { when(it) {
            '<' -> Go.LEFT
            '>' -> Go.RIGHT
            else -> throw IllegalArgumentException()
        } }

    fun run(limit: Int, gos: List<Go>): Int {
        val pieces = PieceIterator(limit)
        val directions = DirectionIterator(gos)

        tailrec fun step(stack: Shape, piece: Shape): Shape {
            if (!pieces.hasNext()) return stack

            val movedLeftRight = piece.move(directions.next(), stack)
                .getOrDefault(piece)
            val movedDownResult = movedLeftRight.move(Direction.DOWN, stack)
            val movedDown = movedDownResult
                .getOrDefault(movedLeftRight)

            return if (movedDownResult.isFailure) {
                val newStack = stack + movedDown
                step(
                    newStack,
                    pieces.next() + Pos(2, newStack.points.maxOf { it.y } + 3 + 1)
                )
            } else {
                step(stack, movedDown)
            }
        }

        val stack = step(Shape(emptySet()), pieces.next() + Pos(2, 3))
        return stack.points.maxOf { it.y } + 1
    }

    override fun part1(input: String): Any {
        val gos = parseInput(input)
        return run(2022, gos)
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
            >>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>
        """.trimIndent()

        assertEquals(17, run(10, parseInput(input)))
        assertEquals(3068, part1(input))
    }

}