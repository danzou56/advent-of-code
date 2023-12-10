package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

internal class Day25 : AdventTestRunner21("Sea Cucumber") {
    override val timeout = Duration.ofMinutes(10)

    enum class Seafloor(val repr: Char) {
        EMPTY('.'), EAST('>'), SOUTH('v')
    }

    override fun part1(input: String): Any {
        val seafloor = input.asMatrix {
            when (it) {
                '>' -> Seafloor.EAST
                'v' -> Seafloor.SOUTH
                else -> Seafloor.EMPTY
            }
        }

        fun step(seafloor: Matrix<Seafloor>, target: Seafloor, dir: Compass): Matrix<Seafloor> {
            val moveable = seafloor.indices2D
                .filter { seafloor[it] == target }
                .filter {
                    seafloor[(it + dir.dir).let { (x, y) ->
                        x % seafloor[0].size to y % seafloor.size
                    }] == Seafloor.EMPTY
                }
            val targets = moveable.map { it + dir.dir }.map { (x, y) ->
                x % seafloor[0].size to y % seafloor.size
            }
            return seafloor.mapIndexed2D { pos, cur ->
                when (pos) {
                    in moveable -> Seafloor.EMPTY
                    in targets -> target
                    else -> cur
                }
            }
        }

        fun step(seafloor: Matrix<Seafloor>): Matrix<Seafloor> =
            step(step(seafloor, Seafloor.EAST, Compass.EAST), Seafloor.SOUTH, Compass.SOUTH)

        tailrec fun simulate(seafloor: Matrix<Seafloor>, steps: Int = 0): Int {
            val res = step(seafloor)
            return if (seafloor == res) steps
            else simulate(res, steps + 1)
        }

        return simulate(seafloor) + 1
    }

    override fun part2(input: String): Any = "Congratulations!"

    @Test
    fun testExample() {
        val input = """
            v...>>.vv>
            .vv>>.vv..
            >>.>v>...v
            >>v>>.>.v.
            v>v.vv.v..
            >.>>..v...
            .vv..>.>v.
            v.v..>>v.v
            ....v..v.>
        """.trimIndent()

        assertEquals(58, part1(input))
    }
}