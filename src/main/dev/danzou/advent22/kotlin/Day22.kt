package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals

typealias Pose = Pair<Pos, Compass>

internal class Day22 : AdventTestRunner22("Monkey Map") {

    val Pose.pos
        get() = this.first
    val Pose.dir
        get() = this.second
    val Pose.x
        get() = this.pos.x
    val Pose.y
        get() = this.pos.y

    private val clockwiseTurnFrom = mapOf(
        Compass.WEST to Compass.NORTH,
        Compass.NORTH to Compass.EAST,
        Compass.EAST to Compass.SOUTH,
        Compass.SOUTH to Compass.WEST,
    )
    private val counterClockwiseTurnFrom = clockwiseTurnFrom.map { (k, v) -> v to k }.toMap()

    fun move2d(board: SparseMatrix<BoardCell>, instructions: List<Instruction>): Pose {
        val start =
            board.filter { (pos, cell) -> pos.y == 0 && cell == BoardCell.EMPTY }.minBy { (pos, _) -> pos.x }.key

        tailrec fun step(pose: Pose, instructions: List<Instruction>): Pose {
            if (instructions.isEmpty()) return pose

            return when (val next = instructions.first()) {
                is Instruction.Move ->
                    if (next.steps <= 0) step(pose, instructions.drop(1))
                    else when (board[pose.pos + pose.dir.dir]) {
                        BoardCell.EMPTY -> step(
                            Pose(pose.pos + pose.dir.dir, pose.dir),
                            listOf(Instruction.Move(next.steps - 1)) + instructions.drop(1)
                        )
                        BoardCell.WALL -> step(
                            pose,
                            instructions.drop(1)
                        )
                        null -> when (pose.dir) {
                            Compass.WEST -> board.filter { (pos, _) -> pos.y == pose.y }.maxBy { (pos, _) -> pos.x }
                            Compass.EAST -> board.filter { (pos, _) -> pos.y == pose.y }.minBy { (pos, _) -> pos.x }
                            Compass.NORTH -> board.filter { (pos, _) -> pos.x == pose.x }.maxBy { (pos, _) -> pos.y }
                            Compass.SOUTH -> board.filter { (pos, _) -> pos.x == pose.x }.minBy { (pos, _) -> pos.y }
                            else -> throw IllegalArgumentException()
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

        return step(Pose(start, Compass.EAST), instructions)
    }

    fun getBoard(input: String): SparseMatrix<BoardCell> =
        input.split("\n").dropLast(2).flatMapIndexed { y, row ->
            row.mapIndexedNotNull { x, c ->
                when (c) {
                    '#' -> Pos(x, y) to BoardCell.WALL
                    '.' -> Pos(x, y) to BoardCell.EMPTY
                    else -> null
                }
            }
        }.toMap()

    fun getInstructions(input: String): List<Instruction> =
        tokenize(input.split("\n").last())

    fun calculatePassword(pose: Pose): Int = listOf(
        (pose.x + 1) * 4,
        (pose.y + 1) * 1000,
        listOf(
            Compass.EAST,
            Compass.SOUTH,
            Compass.WEST,
            Compass.NORTH
        ).indexOf(pose.dir)
    ).sum()

    override fun part1(input: String): Int {
        val pose = move2d(getBoard(input), getInstructions(input))
        return calculatePassword(pose)
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

        assertEquals(Pose(Pos(7, 5), Compass.EAST), move2d(board, instructions))
        assertEquals(6032, part1(input))
    }

    enum class BoardCell {
        EMPTY, WALL;
    }

    sealed class Instruction {
        data class Move(val steps: Int) : Instruction()
        data object ClockwiseTurn : Instruction()
        data object CounterClockwiseTurn : Instruction()
    }

    fun tokenize(line: String): List<Instruction> {
        val toks = mutableListOf<Instruction>()
        tailrec fun step(line: String) {
            if (line.isEmpty()) return
            toks.add(
                when (line.first()) {
                    'R' -> Instruction.ClockwiseTurn
                    'L' -> Instruction.CounterClockwiseTurn
                    else -> {
                        val res = line.takeWhile { it.isDigit() }
                        toks.add(Instruction.Move(res.toInt()))
                        return step(line.drop(res.length))
                    }
                }
            )
            return step(line.drop(1))
        }

        step(line)
        return toks
    }
}