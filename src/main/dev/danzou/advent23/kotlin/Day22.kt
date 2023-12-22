package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.Pos3
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent.utils.geometry3.x
import dev.danzou.advent.utils.geometry3.y
import dev.danzou.advent.utils.geometry3.z
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min

internal class Day22 : AdventTestRunner23() {

    data class Brick(val corner1: Pos3, val corner2: Pos3, val name: String = "") {
        fun floor(): Int = min(corner1.z, corner2.z)
        fun ceil(): Int = max(corner1.z, corner2.z)
        fun left(): Int = min(corner1.x, corner2.x)
        fun right(): Int = max(corner1.x, corner2.x)
        fun bottom(): Int = min(corner1.y, corner2.y)
        fun top(): Int = max(corner1.y, corner2.y)

        fun intersects(other: Brick): Boolean =
            (this.floor()..this.ceil()).intersects(other.floor()..other.ceil()) &&
                    (this.left()..this.right()).intersects(other.left()..other.right()) &&
                    (this.bottom()..this.top()).intersects(other.bottom()..other.top())
    }

    override fun part1(input: String): Int {
        val bricks = input.split("\n")
            .map {
                it.split("~", "   <- ").map {
                    it.split(",")
                }
            }
            .map { els ->
                if (els.size == 2) {
                    Brick(els[0].map(String::toInt).toTriple(), els[1].map(String::toInt).toTriple())
                } else if (els.size == 3) {
                    Brick(els[0].map(String::toInt).toTriple(), els[1].map(String::toInt).toTriple(), els[2].single())
                } else {
                    throw IllegalArgumentException()
                }
            }.sortedBy { it.floor() }

        val stacked = mutableListOf<Brick>()
        bricks.onEach { move ->
            val topBrick = stacked.filter { obstacle ->
                (obstacle.left()..obstacle.right()).intersects(move.left()..move.right()) &&
                        (obstacle.bottom()..obstacle.top()).intersects(move.bottom()..move.top())
            }.maxByOrNull { it.ceil() }
            val top = topBrick?.ceil() ?: -1
            val diff = move.floor() - top - 1
            assert(move.floor() - diff == top + 1)
            val corner1 = move.corner1.copy(third = move.corner1.z - diff)
            val corner2 = move.corner2.copy(third = move.corner2.z - diff)
            val newBrick = move.copy(corner1 = corner1, corner2 = corner2)
            require(topBrick == null || !newBrick.intersects(topBrick))
            stacked.add(newBrick)
            stacked.sortedBy { it.ceil() }
        }

        assert(stacked.size == bricks.size)
//        require(stacked.pairs().all { (b1, b2) ->
//            !b1.intersects(b2)
//        })

        val supportedBy = mutableMapOf<Brick, List<Brick>>()
        stacked.onEach { brick ->
            val supporting = stacked.filter { supporting ->
                (supporting.left()..supporting.right()).intersects(brick.left()..brick.right()) &&
                        (supporting.bottom()..supporting.top()).intersects(brick.bottom()..brick.top()) &&
                        supporting.ceil() + 1 == brick.floor()
            }
            supportedBy[brick] = supporting
        }

        val supporting = supportedBy.flatMap { (brick, supports) ->
            supports.map { support -> brick to support }
        }.groupBy { (_, support) ->
            support
        }.mapValues { (support, aboves: List<Pair<Brick, Brick>>) ->
            aboves.map { (above, support) -> above }
        }

        return stacked.count { brick ->
            (supporting[brick] ?: emptyList()).all { above ->
                (supportedBy[above]!! - brick).isNotEmpty()
            }.also {
//                println("${brick.name} supports ${supporting[brick]?.map(Brick::name)}")
            }
        }
    }// not 576

    override fun part2(input: String): Any {
        val bricks = input.split("\n")
            .map {
                it.split("~", "   <- ").map {
                    it.split(",")
                }
            }
            .map { els ->
                if (els.size == 2) {
                    Brick(els[0].map(String::toInt).toTriple(), els[1].map(String::toInt).toTriple())
                } else if (els.size == 3) {
                    Brick(els[0].map(String::toInt).toTriple(), els[1].map(String::toInt).toTriple(), els[2].single())
                } else {
                    throw IllegalArgumentException()
                }
            }.sortedBy { it.floor() }

        val stacked = mutableListOf<Brick>()
        bricks.onEach { move ->
            val topBrick = stacked.filter { obstacle ->
                (obstacle.left()..obstacle.right()).intersects(move.left()..move.right()) &&
                        (obstacle.bottom()..obstacle.top()).intersects(move.bottom()..move.top())
            }.maxByOrNull { it.ceil() }
            val top = topBrick?.ceil() ?: -1
            val diff = move.floor() - top - 1
            assert(move.floor() - diff == top + 1)
            val corner1 = move.corner1.copy(third = move.corner1.z - diff)
            val corner2 = move.corner2.copy(third = move.corner2.z - diff)
            val newBrick = move.copy(corner1 = corner1, corner2 = corner2)
            require(topBrick == null || !newBrick.intersects(topBrick))
            stacked.add(newBrick)
            stacked.sortedBy { it.ceil() }
        }

        assert(stacked.size == bricks.size)
//        require(stacked.pairs().all { (b1, b2) ->
//            !b1.intersects(b2)
//        })

        val supportedBy = mutableMapOf<Brick, List<Brick>>()
        stacked.onEach { brick ->
            val supporting = stacked.filter { supporting ->
                (supporting.left()..supporting.right()).intersects(brick.left()..brick.right()) &&
                        (supporting.bottom()..supporting.top()).intersects(brick.bottom()..brick.top()) &&
                        supporting.ceil() + 1 == brick.floor()
            }
            supportedBy[brick] = supporting
        }

        val supporting = supportedBy.flatMap { (brick, supports) ->
            supports.map { support -> brick to support }
        }.groupBy { (_, support) ->
            support
        }.mapValues { (support, aboves: List<Pair<Brick, Brick>>) ->
            aboves.map { (above, support) -> above }
        }

        val soleSupports = stacked.associateWith { brick ->
            (supporting[brick] ?: emptyList()).count { above ->
                (supportedBy[above]!! - brick).isEmpty()
            }
        }

        return soleSupports.filter { (_, count) -> count > 0 }
            .entries
            .sumOf { (brick, _) ->
                bricksToFall(supporting, supportedBy, setOf(brick), setOf(brick))
            }
    }

    fun bricksToFall(supporting: Map<Brick, List<Brick>>, supportedBy: Map<Brick, List<Brick>>, missing: Set<Brick>, bricks: Set<Brick>): Int {
        val aboves = bricks.flatMap { supporting[it] ?: emptyList() }
        if (aboves.isEmpty()) return 0
        val willFall = aboves.filter { above ->
            (supportedBy[above] ?: emptyList()).all { it in missing }
        }.toSet()
        return willFall.size + bricksToFall(supporting, supportedBy, missing + willFall, willFall)
    }

    @Test
    fun testExample() {
        val input = """
            1,0,1~1,2,1   <- A
            0,0,2~2,0,2   <- B
            0,2,3~2,2,3   <- C
            0,0,4~0,2,4   <- D
            2,0,5~2,2,5   <- E
            0,1,6~2,1,6   <- F
            1,1,8~1,1,9   <- G
        """.trimIndent()

        assertEquals(5, part1(input))
        assertEquals(7, part2(input))
    }
}