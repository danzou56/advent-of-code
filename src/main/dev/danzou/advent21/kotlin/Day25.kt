package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day25 : AdventTestRunner21("Sea Cucumber") {
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
        }.map { it.toMutableList() }.toMutableList()

        fun step(seafloor: MutableMatrix<Seafloor>, target: Seafloor, dir: Compass): Boolean =
            seafloor.indices2D
                .filter { seafloor[it] == target }
                .map {
                    it to (it + dir.dir).let { (x, y) ->
                        x % seafloor[0].size to y % seafloor.size
                    }
                }
                .filter { (_, next) -> seafloor[next] == Seafloor.EMPTY }
                .onEach { (it, next) ->
                    seafloor[it] = Seafloor.EMPTY
                    seafloor[next] = target
                }
                .isNotEmpty()

        fun step(seafloor: MutableMatrix<Seafloor>): Boolean {
            // no short-circuiting allowed
            val move1 = step(seafloor, Seafloor.EAST, Compass.EAST)
            val move2 = step(seafloor, Seafloor.SOUTH, Compass.SOUTH)
            return move1 || move2
        }

        tailrec fun simulate(seafloor: MutableMatrix<Seafloor>, steps: Int = 0): Int {
            return if (!step(seafloor)) steps
            else simulate(seafloor, steps + 1)
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