package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.*
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.max

typealias Piece = RectangleUnion

class Day17 : AdventTestRunner22() {

    enum class Go(val dir: Direction) {
        LEFT(Direction.LEFT), RIGHT(Direction.RIGHT);
    }

    enum class BasePiece(val baseShape: RectangleUnion) {
        Horizontal(Rectangle(Point(0, 0), Point(3, 0))),
        Vertical(Rectangle(Point(0, 0), Point(0, 3))),
        Plus(RectangleUnion.fromRectangles(
                Rectangle(Point(0, 1), Point(2, 1)),
                Rectangle(Point(1, 0), Point(1, 2)),
        )),
        RightL(RectangleUnion.fromRectangles(
            Rectangle(Point(0, 0), Point(2, 0)),
            Rectangle(Point(2, 0), Point(2, 2)),
        )),
//        LeftL(),
        Square(Rectangle(Point(0, 0), Point(1, 1))),
    }

    class PieceIterator(private val limit: Int) : Iterator<BasePiece> {
         private val pieceSequence = arrayOf(
            BasePiece.Horizontal,
            BasePiece.Plus,
            BasePiece.RightL,
            BasePiece.Vertical,
            BasePiece.Square
        )
        private var cur = 0

        override fun hasNext(): Boolean = cur <= limit
        override fun next(): BasePiece = pieceSequence[cur++.also { /*println(cur)*/ } % pieceSequence.size]
    }

    class DirectionIterator(private val directions: List<Go>) : Iterator<Direction> {
        private var cur = 0
            set(value) {
                field = value % directions.size
            }

        override fun hasNext(): Boolean = true
        override fun next(): Direction {
//            if (cur == 0) println("hit end")
            return directions[cur++].dir
        }
    }

    val cave = object : Rectangle(Pos(0, 0), Pos(6, 6)) {
        private var _height = 7
        override val height: Int
            get() = _height

        override fun contains(p: Pos): Boolean {
            _height = max(_height, p.y)
            return p.y >= 0 && p.x in 0 until width
        }

        override fun contains(other: Polygon): Boolean =
            this.contains(other.pos) && this.contains(other.pos + other.size - Pos(1, 1))
    }

    fun parseInput(input: String): List<Go> =
        input.map { when(it) {
            '<' -> Go.LEFT
            '>' -> Go.RIGHT
            else -> throw IllegalArgumentException()
        } }

    fun drop(piece: BasePiece): RectangleUnion = piece.baseShape
    infix fun RectangleUnion.at(p: Pos): RectangleUnion = this + p
    infix fun RectangleUnion.onto(stack: Polygon): RectangleUnion {
        TODO()
    }

    fun run(limit: Int, gos: List<Go>): Int {
        val pieces = PieceIterator(limit)
        val directions = DirectionIterator(gos)
        tailrec fun step(stack: Polygon, piece: RectangleUnion): Polygon {
            printStack(stack.union(piece) as RectangleUnion)
//            if (stack.height == 3068) return stack
            if (!pieces.hasNext()) return stack
            val movedHorizontally = (piece + directions.next().dir).let {
                if (cave.contains(it) && !stack.intersects(it)) it
                else piece
            }
            val verticalMove = movedHorizontally + Direction.DOWN.dir
            val canMoveDown = cave.contains(verticalMove) && !stack.intersects(verticalMove)

            if (!canMoveDown) {
                val newStack = stack.union(movedHorizontally)
//                println("Previous piece dropped at ${movedHorizontally.pos}")
                val nextPiece = drop(pieces.next().also { /*print(it.name)*/ }) at Pos(2, newStack.height + 3)
//                println(" dropping at ${nextPiece.pos}")
                return step(newStack, nextPiece)
            } else {
                return step(stack, verticalMove)
            }
        }

        val stack = step(EmptyPolygon(), drop(pieces.next()) at Pos(2, 3))
        println((stack as RectangleUnion).components)
        assert((stack as RectangleUnion).components == limit)
        return stack.height
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

    fun printStack(stack: RectangleUnion) {
        return
        var level = 0
        val res = mutableListOf("+-------+")
        while (res.size < 5 || !res.take(4).all { it == "|.......|" }) {
            res.add(0, "|" + (0..6).map {
                if (stack.contains(Pos(it, level))) '#'
                else '.'
            }.joinToString("") + "|")
            level++
        }
        println(res.joinToString("\n"))
    }
}