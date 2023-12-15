package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day20 : AdventTestRunner21("Trench Map") {
    enum class PixelState(val char: Char) {
        LIT('#'), DARK('.');

        operator fun not(): PixelState = if (this == LIT) DARK else LIT
    }

    data class Image(val background: PixelState, val pixels: Set<Pos>) {
        operator fun get(pos: Pos): PixelState =
            if (pos in pixels) !background
            else background

        // @formatter:off
        private val surroundingDirs = listOf(
            Compass.NORTHWEST, Compass.NORTH,  Compass.NORTHEAST,
            Compass.WEST,      Compass.CENTER, Compass.EAST,
            Compass.SOUTHWEST, Compass.SOUTH,  Compass.SOUTHEAST
        ).map(Compass::dir)
        // @formatter:on
        fun getSurroundingPos(pos: Pos): List<Pos> =
            surroundingDirs.map { it + pos }

        fun getSurrounding(pos: Pos): List<PixelState> =
            getSurroundingPos(pos).map(::get)

        fun enhance(algorithm: Algorithm): Image {
            val toVisit = pixels.flatMap {
                getSurroundingPos(it)
            }.distinct()
            val background = algorithm[this.background]
            return Image(background, toVisit.filter {
                algorithm[getSurroundingPos(it).map(::get)] != background
            }.toSet())
        }

        override fun toString(): String {
            val leftBound = pixels.minOf { it.x }
            val rightBound = pixels.maxOf { it.x }
            val lowerBound = pixels.minOf { it.y }
            val upperBound = pixels.maxOf { it.y }

            return (lowerBound..upperBound).map { y ->
                (leftBound..rightBound).map { x ->
                    get(Pos(x, y)).char
                }.joinToString("")
            }.joinToString("\n")
        }

        companion object {
            fun fromInput(input: String): Image {
                val rawImage = input.split("\n").drop(2)
                    .map(String::toList)
                val litPixels = rawImage.mapIndexed2D { p, char ->
                    Pair(p, char == PixelState.LIT.char)
                }.flatten().filter { it.second }.map { it.first }
                assert(litPixels.size == litPixels.toSet().size)
                return Image(PixelState.DARK, litPixels.toSet())
            }
        }
    }

    class Algorithm(private val algorithm: List<PixelState>) {
        constructor(input: String) : this(
            PixelState.values().associateBy({ it.char }, { it })
                .let { charToPixelState ->
                    input.map { charToPixelState[it]!! }
                }
        ) {
            assert(algorithm.size == 512)
        }

        operator fun get(pixel: PixelState): PixelState =
            when (pixel) {
                PixelState.DARK -> algorithm.first()
                PixelState.LIT -> algorithm.last()
            }

        operator fun get(pixels: List<PixelState>): PixelState =
            algorithm[pixels.map {
                when (it) {
                    PixelState.LIT -> "1"
                    PixelState.DARK -> "0"
                }
            }.joinToString("").toInt(2)]

    }

    override fun part1(input: String): Any {
        val image = Image.fromInput(input)
        val algorithm = Algorithm(input.split("\n").first())

        return image.enhance(algorithm).enhance(algorithm).pixels.size
    }

    override fun part2(input: String): Any {
        val image = Image.fromInput(input)
        val algorithm = Algorithm(input.split("\n").first())

        return 50.times(image) { it.enhance(algorithm) }.pixels.size
    }

    @Test
    fun testExample() {
        val input = """
            ..#.#..#####.#.#.#.###.##.....###.##.#..###.####..#####..#....#..#..##..###..######.###...####..#..#####..##..#.#####...##.#.#..#.##..#.#......#.###.######.###.####...#.##.##..#..#..#####.....#.#....###..#.##......#.....#..#..#..##..#...##.######.####.####.#.#...#.......#..#.#.#...####.##.#......#..#...##.#.##..#...##.#.##..###.#......#.#.......#.#.#.####.###.##...#.....####.#..#..#.##.#....##..#.####....##...##..#...#......#.#.......#.......##..####..#...#.#.#...##..#.#..###..#####........#..####......#..#

            #..#.
            #....
            ##..#
            ..#..
            ..###
        """.trimIndent()

        assertEquals(35, part1(input))
    }
}