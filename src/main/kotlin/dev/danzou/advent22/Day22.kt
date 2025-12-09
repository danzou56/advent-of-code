package dev.danzou.advent22

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals

private typealias Pose = Pair<Pos, Compass>

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

    /**
     * Maps an invalid source coordinate to a valid destination
     */
    data class EdgeWrapping(
        val source: List<Pose>,
        val destination: List<Pose>
    ) {
        private val wrap = source.zip(destination).toMap()
        private val _source = source.toSet()

        init {
            require(source.size == destination.size)
        }

        infix operator fun contains(pose: Pose): Boolean = pose in _source
        operator fun invoke(pose: Pose): Pose {
            return wrap[pose]!!
        }
    }

    fun move3d(board: SparseMatrix<BoardCell>, instructions: List<Instruction>): Pose {
        val start = board.filter { (pos, cell) ->
            pos.y == 0 && cell == BoardCell.EMPTY
        }.keys.minBy(Pos::x)

        // Hard coded against the example input
        val faceSize = 50
        val faceOffsets = 0..<faceSize

        // Hard coding of how to wrap over the side of the cube
        //      6W<-1N 2N->6S
        //          ┌─┬─┐
        //   4W<-1W │1│2│ 2E->5E
        //          ├─┼─┘
        //  4N<->3W │3│ 3E<->2S
        //        ┌─┼─┤
        // 1W<-4W │4│5│ 5E->2E
        //        ├─┼─┘
        // 1N<-6W │6│ 6E<->5S
        //        └─┘
        //        6S->2N
        val edgeMappings = mapOf(
            "1N->6W" to EdgeWrapping(
                faceOffsets.map { (faceSize + it to -1) to Compass.NORTH },
                faceOffsets.map { (0 to 3 * faceSize + it) to Compass.EAST }
            ),
            "6W->1N" to EdgeWrapping(
                faceOffsets.map { (-1 to 3 * faceSize + it) to Compass.WEST },
                faceOffsets.map { (faceSize + it to 0) to Compass.SOUTH },
            ),
            "2N->6S" to EdgeWrapping(
                faceOffsets.map { (2 * faceSize + it to -1) to Compass.NORTH },
                faceOffsets.map { (it to 4 * faceSize - 1) to Compass.NORTH }
            ),
            "6S->2N" to EdgeWrapping(
                faceOffsets.map { (it to 4 * faceSize) to Compass.SOUTH },
                faceOffsets.map { (2 * faceSize + it to 0) to Compass.SOUTH }
            ),
            "2E->5E" to EdgeWrapping(
                faceOffsets.map { (3 * faceSize to it) to Compass.EAST }.reversed(),
                faceOffsets.map { (2 * faceSize - 1 to 2 * faceSize + it) to Compass.WEST }
            ),
            "5E->2E" to EdgeWrapping(
                faceOffsets.map { (2 * faceSize to 2 * faceSize + it) to Compass.EAST },
                faceOffsets.map { (3 * faceSize - 1 to it) to Compass.WEST }.reversed()
            ),
            "2S->3E" to EdgeWrapping(
                faceOffsets.map { (2 * faceSize + it to faceSize) to Compass.SOUTH },
                faceOffsets.map { (2 * faceSize - 1 to faceSize + it) to Compass.WEST }
            ),
            "3E->2S" to EdgeWrapping(
                faceOffsets.map { (2 * faceSize to faceSize + it) to Compass.EAST },
                faceOffsets.map { (2 * faceSize + it to faceSize - 1) to Compass.NORTH },
            ),
            "5S->6E" to EdgeWrapping(
                faceOffsets.map { (faceSize + it to 3 * faceSize) to Compass.SOUTH },
                faceOffsets.map { (faceSize - 1 to 3 * faceSize + it) to Compass.WEST }
            ),
            "6E->5S" to EdgeWrapping(
                faceOffsets.map { (faceSize to 3 * faceSize + it) to Compass.EAST },
                faceOffsets.map { (faceSize + it to 3 * faceSize - 1) to Compass.NORTH },
            ),
            "4W->1W" to EdgeWrapping(
                faceOffsets.map { (-1 to 2 * faceSize + it) to Compass.WEST },
                faceOffsets.map { (faceSize to it) to Compass.EAST }.reversed()
            ),
            "1W->4W" to EdgeWrapping(
                faceOffsets.map { (faceSize - 1 to it) to Compass.WEST }.reversed(),
                faceOffsets.map { (0 to 2 * faceSize + it) to Compass.EAST }
            ),
            "4N->3W" to EdgeWrapping(
                faceOffsets.map { (it to 2 * faceSize - 1) to Compass.NORTH },
                faceOffsets.map { (faceSize to faceSize + it) to Compass.EAST }
            ),
            "3W->4N" to EdgeWrapping(
                faceOffsets.map { (faceSize - 1 to faceSize + it) to Compass.WEST },
                faceOffsets.map { (it to 2 * faceSize) to Compass.SOUTH }
            ),
        ).values.toList()

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
                        // This coordinate is invalid and necessitates wrapping over an edge
                        null -> (pose.pos + pose.dir.dir to pose.dir).let { badPose ->
                            edgeMappings.single { it.contains(badPose) }(badPose)
                        }.let { nextPose ->
                            // Look ahead to make sure next pose is actually valid (can't reuse
                            // above since previous position isn't in scope).
                            when (board[nextPose.pos]) {
                                BoardCell.EMPTY -> step(
                                    nextPose,
                                    listOf(Instruction.Move(next.steps - 1)) + instructions.drop(1)
                                )
                                BoardCell.WALL -> step(
                                    pose,
                                    instructions.drop(1)
                                )
                                null -> throw IllegalStateException()
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
        val pose = move3d(getBoard(input), getInstructions(input))
        return calculatePassword(pose)
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