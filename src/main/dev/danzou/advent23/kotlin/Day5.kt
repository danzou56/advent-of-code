package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

val numRegex = Regex("\\d+")

internal class Day5 : AdventTestRunner23("If You Give A Seed A Fertilizer") {
    override val timeout: Duration = Duration.ofMinutes(10)

    override fun part1(input: String): Long {
        val blocks = input.split("\n\n")
        val seeds = numRegex.findAll(blocks[0]).map { it.value.toLong() }
        val maps = blocks.drop(1).map { block ->
            block.split("\n").drop(1) // drop the title
                .map { numRegex.findAll(it).map { it.value.toLong() }.toList() }
                .map { (dstRangeStart, srcRangeStart, len) ->
                    Pair(srcRangeStart..srcRangeStart + len, dstRangeStart..dstRangeStart + len)
                }

        }
        return seeds.map { seed ->
            maps.fold(seed) { num, map ->
                val (srcRange, dstRange) = map.firstOrNull { (srcRange, dstRange) -> num in srcRange } ?: Pair(
                    null,
                    null
                )
                if (srcRange == null) num
                else dstRange!!.first + (num - srcRange.first)
            }

        }.toList().min()
    }

    override fun part2(input: String): Long {
        val blocks = input.split("\n\n")
        val seedRanges = numRegex.findAll(blocks[0]).map { it.value.toLong() }.toList()
            .windowed(2, step = 2)
            .map { (rangeStart, len) -> rangeStart..<rangeStart + len }
        val maps: List<List<Pair<LongRange, LongRange>>> = blocks.drop(1).map { block ->
            block.split("\n").drop(1) // drop the title
                .map { numRegex.findAll(it).map { it.value.toLong() }.toList() }
                .map { (dstRangeStart, srcRangeStart, len) ->
                    Pair(srcRangeStart..<srcRangeStart + len, dstRangeStart..<dstRangeStart + len)
                }
        }

        return maps.fold(seedRanges) { ranges, srcDstMaps ->
            srcDstMaps.fold<Pair<LongRange, LongRange>, Pair<List<LongRange>, List<LongRange>>>(
                Pair(ranges, emptyList())
            ) { (unacceptedRanges, acceptedRanges), (srcRange, dstRange) ->
                val ranges: List<Pair<List<LongRange>, List<LongRange>>> = unacceptedRanges.map { range ->
                    val intersection = max(range.first, srcRange.first)..min(range.last, srcRange.last)
                    if (intersection.isEmpty()) Pair(listOf(range), emptyList())
                    else Pair(
                        listOfNotNull(
                            (range.first..<intersection.first).takeUnless(LongRange::isEmpty),
                            (intersection.last + 1..range.last).takeUnless(LongRange::isEmpty)
                        ), listOf(
                            dstRange.first + (intersection.first - srcRange.first)..dstRange.first + (intersection.first - srcRange.first) + (intersection.last - intersection.first)
                        )
                    )
                }
                Pair(ranges.flatMap { it.first }, acceptedRanges + ranges.flatMap { it.second })
            }.let { (unacceptedRanges, acceptedRanges) ->
                acceptedRanges + unacceptedRanges
            }
        }.minOf { it.first }
    }

    @Test
    fun testExample() {
        val input = """
            seeds: 79 14 55 13

            seed-to-soil map:
            50 98 2
            52 50 48

            soil-to-fertilizer map:
            0 15 37
            37 52 2
            39 0 15

            fertilizer-to-water map:
            49 53 8
            0 11 42
            42 0 7
            57 7 4

            water-to-light map:
            88 18 7
            18 25 70

            light-to-temperature map:
            45 77 23
            81 45 19
            68 64 13

            temperature-to-humidity map:
            0 69 1
            1 0 69

            humidity-to-location map:
            60 56 37
            56 93 4
        """.trimIndent()

        assertEquals(35L, part1(input))
        assertEquals(46L, part2(input))
    }
}