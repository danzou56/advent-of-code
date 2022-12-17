package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

typealias Beacon = Pos
typealias Sensor = Pos

internal class Day15 : AdventTestRunner() {
    data class Reading(val sensor: Sensor, val beacon: Beacon) {
        val radius = sensor.manhattanDistanceTo(beacon)
        val boundingBox = Rectangle(
            sensor - Pos(radius, radius),
            sensor + Pos(radius, radius)
        )

        companion object {
            fun fromLine(line: String): Reading {
                val (sx, sy, bx, by) = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)\\s*")
                    .matchEntire(line)!!
                    .groupValues
                    .drop(1)
                    .map { it.toInt() }
                return Reading(Sensor(sx, sy), Beacon(bx, by))
            }
        }

        fun contains(p: Pos): Boolean =
            sensor.manhattanDistanceTo(p) <= radius

        fun coveredCells(): Set<Pos> =
            object : Set<Pos> {
                override val size: Int = radius * (radius + 2) + 1

                override fun isEmpty(): Boolean =
                    false

                override fun iterator(): Iterator<Pos> {
                    TODO("Not yet implemented")
                }

                override fun containsAll(elements: Collection<Pos>): Boolean =
                    elements.all { this.contains(it) }

                override fun contains(element: Pos): Boolean =
                    this@Reading.contains(element)
            }
    }

    override fun part1(input: String): Any =
        part1(input, 2_000_000)

    fun part1(input: String, row: Int) : Any {
        val readings = input.split("\n").map { Reading.fromLine(it) }
        val boxes = readings.map { it.boundingBox }
        val minX = boxes.minOf { it.lower(0) }
        val maxX = boxes.maxOf { it.upper(0) }
        val coverings = readings.map { it.coveredCells() }
        val beacons = readings.map { it.beacon }
        return (minX .. maxX).count { x ->
            Pos(x, row) !in beacons && coverings.any { it.contains(Pos(x, row)) }
        }
    }

    override fun part2(input: String): Any =
        part2(input, Rectangle(Pos(0, 0), Pos(4_000_000, 4_000_000)))

    fun part2(input: String, boundingBox: Rectangle): Any {
        val readings = input.split("\n").map { Reading.fromLine(it) }

        val reading = readings.firstNotNullOf {
            (1..it.radius).firstNotNullOfOrNull { i ->
                listOf(
                    Pair(it.sensor.x - it.radius + i, it.sensor.y + i + 1),
                    Pair(it.sensor.x - it.radius + i, it.sensor.y - i - 1),
                    Pair(it.sensor.x + it.radius - i, it.sensor.y + i + 1),
                    Pair(it.sensor.x + it.radius - i, it.sensor.y - i - 1),
                )
                    .filter { boundingBox.contains(it) }
                    .firstOrNull {
                        readings.none { reading -> reading.contains(it) }
                    }
            }
        }

        return reading.y + (reading.x * 4_000_000L)
    }

    @Test
    fun testExample() {
        val input = """
            Sensor at x=2, y=18: closest beacon is at x=-2, y=15
            Sensor at x=9, y=16: closest beacon is at x=10, y=16
            Sensor at x=13, y=2: closest beacon is at x=15, y=3
            Sensor at x=12, y=14: closest beacon is at x=10, y=16
            Sensor at x=10, y=20: closest beacon is at x=10, y=16
            Sensor at x=14, y=17: closest beacon is at x=10, y=16
            Sensor at x=8, y=7: closest beacon is at x=2, y=10
            Sensor at x=2, y=0: closest beacon is at x=2, y=10
            Sensor at x=0, y=11: closest beacon is at x=2, y=10
            Sensor at x=20, y=14: closest beacon is at x=25, y=17
            Sensor at x=17, y=20: closest beacon is at x=21, y=22
            Sensor at x=16, y=7: closest beacon is at x=15, y=3
            Sensor at x=14, y=3: closest beacon is at x=15, y=3
            Sensor at x=20, y=1: closest beacon is at x=15, y=3
        """.trimIndent()

        assertEquals(26, part1(input, 10))
        assertEquals(56000011L, part2(input, Rectangle(Pos(0, 0), Pos(20, 20))))
    }
}