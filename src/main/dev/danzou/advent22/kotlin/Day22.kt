package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Direction
import dev.danzou.advent.utils.geometry.plus
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals

typealias Pose = Pair<Pos, Direction>
val Pose.pos
    get() = this.first
val Pose.dir
    get() = this.second
val Pose.x
    get() = this.pos.x
val Pose.y
    get() = this.pos.y

internal class Day22 : AdventTestRunner() {

    private val clockwiseTurnFrom = mapOf(
        Direction.LEFT to Direction.UP,
        Direction.UP to Direction.RIGHT,
        Direction.RIGHT to Direction.DOWN,
        Direction.DOWN to Direction.LEFT,
    )
    private val counterClockwiseTurnFrom = clockwiseTurnFrom.map { (k, v) -> v to k }.toMap()

    fun move(board: SparseMatrix<BoardCell>, instructions: List<Instruction>): Pose {
        val start = board.filter { (pos, cell) -> pos.y == 0 && cell == BoardCell.EMPTY }.minBy { (pos, _) -> pos.x }.key
        tailrec fun step(pose: Pose, instructions: List<Instruction>): Pose {
            if (instructions.isEmpty()) return pose

            return when (val next = instructions.first()) {
                is Instruction.Move ->
                    if (next.steps <= 0) step(pose, instructions.drop(1))
                    else when (board[pose.pos + pose.dir.invDir]) {
                        BoardCell.EMPTY -> step(
                            Pose(pose.pos + pose.dir.invDir, pose.dir),
                            listOf(Instruction.Move(next.steps - 1)) + instructions.drop(1)
                        )

                        BoardCell.WALL -> step(
                            pose,
                            instructions.drop(1)
                        )

                        null -> when (pose.dir) {
                            Direction.LEFT -> board.filter { (pos, _) -> pos.y == pose.pos.y }
                                .maxBy { (pos, _) -> pos.x }

                            Direction.RIGHT -> board.filter { (pos, _) -> pos.y == pose.pos.y }
                                .minBy { (pos, _) -> pos.x }

                            Direction.UP -> board.filter { (pos, _) -> pos.x == pose.pos.x }.maxBy { (pos, _) -> pos.y }
                            Direction.DOWN -> board.filter { (pos, _) -> pos.x == pose.pos.x }
                                .minBy { (pos, _) -> pos.y }
                        }.let { (pos, cell) ->
                            when (cell) {
                                BoardCell.WALL -> step(
                                    pose,
                                    instructions.drop(1)
                                )

                                BoardCell.EMPTY -> step(
                                    Pose(pos, pose.dir),
                                    listOf(Instruction.Move(next.steps - 1)) + instructions.drop(1)
                                )
                            }
                        }
                    }

                is Instruction.ClockwiseTurn -> step(
                    Pose(pose.pos, clockwiseTurnFrom[pose.dir]!!),
                    instructions.drop(1)
                )

                is Instruction.CounterClockwiseTurn -> step(
                    Pose(pose.pos, counterClockwiseTurnFrom[pose.dir]!!),
                    instructions.drop(1)
                )
            }
        }

        return step(Pose(start, Direction.RIGHT), instructions)
    }

    fun getBoard(input: String): SparseMatrix<BoardCell> =
        input.split("\n").dropLast(2).flatMapIndexed { y, row -> row.mapIndexedNotNull { x, c ->
            when (c) {
                '#' -> Pos(x, y) to BoardCell.WALL
                '.' -> Pos(x, y) to BoardCell.EMPTY
                else -> null
            }
        }}.toMap()

    fun getInstructions(input: String): List<Instruction> =
        tokenize(input.split("\n").last())

    override fun part1(input: String): Any {
        val pose = move(getBoard(input), getInstructions(input))
        return listOf(
            (pose.x + 1) * 4,
            (pose.y + 1) * 1000,
            listOf(
                Direction.RIGHT,
                Direction.DOWN,
                Direction.LEFT,
                Direction.UP
            ).indexOf(pose.dir)).sum()
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
                    ...#
                    .#..
                    #...
                    ....
            ...#.......#
            ........#...
            ..#....#....
            ..........#.
                    ...#....
                    .....#..
                    .#......
                    ......#.
            
            10R5L5R10L4R5L5
        """.trimIndent()

        val board = getBoard(input)
        val instructions = getInstructions(input)

        assertEquals(Pose(Pos(7, 5), Direction.RIGHT), move(board, instructions))
        assertEquals(6032, part1(input))
    }

    enum class BoardCell {
        EMPTY, WALL;
    }

    sealed class Instruction {
        class Move(val steps: Int) : Instruction() {
            override fun toString(): String {
                return "${super.toString()}($steps)"
            }
        }
        object ClockwiseTurn : Instruction()
        object CounterClockwiseTurn : Instruction()

        override fun toString(): String {
            return this.javaClass.simpleName
        }
    }

    fun tokenize(line: String): List<Instruction> {
        val toks = mutableListOf<Instruction>()
        fun step(line: String) {
            if (line.isEmpty()) return
            toks.add(when (line.first()) {
                'R' -> Instruction.ClockwiseTurn
                'L' -> Instruction.CounterClockwiseTurn
                else -> {
                    val res = Regex("""^\d+""").find(line) ?: throw IllegalArgumentException()
                    toks.add(Instruction.Move(res.value.toInt()))
                    return step(line.drop(res.value.length))
                }
            })
            return step(line.drop(1))
        }

        step(line)
        return toks
    }
}