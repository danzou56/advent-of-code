package dev.danzou.advent23

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.Pos3
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent.utils.geometry3.x
import dev.danzou.advent.utils.geometry3.y
import dev.danzou.advent.utils.geometry3.z
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min

internal class Day22 : AdventTestRunner23("Sand Slabs") {

    data class Brick(val corner1: Pos3, val corner2: Pos3) {
        val left: Int = min(corner1.x, corner2.x)
        val right: Int = max(corner1.x, corner2.x)
        val x = left..right
        val bottom: Int = min(corner1.y, corner2.y)
        val top: Int = max(corner1.y, corner2.y)
        val y = bottom..top
        val floor: Int = min(corner1.z, corner2.z)
        val ceil: Int = max(corner1.z, corner2.z)
        val z = floor..ceil

        fun intersects(other: Brick): Boolean =
            this.x.intersects(other.x) && this.y.intersects(other.y) && this.z.intersects(other.z)

        companion object {
            val FLOOR = Brick(Triple(Int.MIN_VALUE, Int.MIN_VALUE, -1), Triple(Int.MAX_VALUE, Int.MAX_VALUE, -1))
            fun fromString(input: String): Brick =
                input.split("~").map { it.split(",").map(String::toInt).toTriple() }.let { (c1, c2) -> Brick(c1, c2) }
        }
    }

    fun turnGravityOn(bricks: List<Brick>): Pair<Map<Brick, List<Brick>>, Map<Brick, List<Brick>>> {
        val stacked = mutableListOf<Brick>()
        bricks.sortedBy { it.floor }.onEach { fall ->
            val top = stacked.lastOrNull { obstacle ->
                obstacle.x.intersects(fall.x) && obstacle.y.intersects(fall.y)
            } ?: Brick.FLOOR
            val diff = fall.floor - top.ceil - 1
            val landed = fall.copy(
                corner1 = fall.corner1.copy(third = fall.corner1.z - diff),
                corner2 = fall.corner2.copy(third = fall.corner2.z - diff)
            )
            assert(!landed.intersects(top))

            val insertAt = stacked.binarySearch(landed, compareBy(Brick::ceil)).let {
                if (it < 0) -it - 1
                else it
            }
            stacked.add(insertAt, landed)
        }

        // Map from bricks to bricks that are supporting it (i.e. below)
        val supportedBy = mutableMapOf<Brick, List<Brick>>()
        stacked.onEach { brick ->
            val supporting = stacked.filter { supporting ->
                supporting.ceil + 1 == brick.floor && supporting.x.intersects(brick.x) && supporting.y.intersects(brick.y)
            }
            supportedBy[brick] = supporting
        }

        // Map from bricks to bricks that it supports (i.e. above)
        val supporting = supportedBy.flatMap { (brick, supports) ->
            supports.map { support -> brick to support }
        }.groupBy { (_, support) ->
            support
        }.mapValues { (_, aboves) ->
            aboves.map { (above, _) -> above }
        }
        return supporting to supportedBy
    }

    override fun part1(input: String): Int {
        val bricks = input.split("\n").map(Brick.Companion::fromString)
        val (supporting, supportedBy) = turnGravityOn(bricks)

        // The supporting map doesn't contain all bricks (as some bricks do not support anything)
        // so iterate over supportedBy to make sure all bricks are counted
        return supportedBy.keys.count { brick ->
            (supporting[brick] ?: emptyList()).all { above ->
                (supportedBy[above]!! - brick).isNotEmpty()
            }
        }
    }

    override fun part2(input: String): Int {
        val bricks = input.split("\n").map(Brick.Companion::fromString)
        val (supporting, supportedBy) = turnGravityOn(bricks)

        tailrec fun bricksToFall(removed: Set<Brick>, missing: Set<Brick>): Int {
            if (removed.isEmpty()) return missing.size

            val unsupported = removed
                .flatMap { supporting[it] ?: emptyList() }
                .filter { above ->
                    supportedBy[above]!!.all { it in missing || it in removed }
                }.toSet()
            return bricksToFall(unsupported, missing + removed)
        }

        return supportedBy.keys.sumOf { brick ->
            bricksToFall(setOf(brick), emptySet()) - 1
        }
    }

    @Test
    fun testExample() {
        val input = """
            1,0,1~1,2,1
            0,0,2~2,0,2
            0,2,3~2,2,3
            0,0,4~0,2,4
            2,0,5~2,2,5
            0,1,6~2,1,6
            1,1,8~1,1,9
        """.trimIndent()

        assertEquals(5, part1(input))
        assertEquals(7, part2(input))
    }
}