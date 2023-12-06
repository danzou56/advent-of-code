package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import dev.danzou.advent.utils.intersect

internal class Day5 : AdventTestRunner23("If You Give A Seed A Fertilizer") {
    data class AlmanacMap(val source: LongRange, val offset: Long)
    data class Almanac(val maps: List<AlmanacMap>)

    fun getAlmanacs(input: String): List<Almanac> =
        input.split("\n\n").drop(1).map { block ->
            block.split("\n").drop(1)
                .map { line -> line.split(" ").map { it.toLong() } }
                .map { (destination, source, length) ->
                    AlmanacMap(source..<source + length, destination - source)
                }
        }.map { Almanac(it) }

    override fun part1(input: String): Long {
        val seeds = input.takeWhile { it != '\n' }
            .split(" ")
            .drop(1)
            .map { it.toLong() }
        val almanacs = getAlmanacs(input)

        val locations = almanacs.fold(seeds) { seeds, (almanacMaps) ->
            seeds.map { seed ->
                seed + (almanacMaps.firstOrNull { (source, _) -> seed in source }?.offset ?: 0)
            }
        }

        return locations.min()
    }

    override fun part2(input: String): Long {
        val seedRanges = input.takeWhile { it != '\n' }
            .split(" ")
            .drop(1)
            .map { it.toLong() }
            .windowed(2, step = 2)
            .map { (start, len) -> start..<start + len }
        val almanacs = getAlmanacs(input)

        val locationRanges = almanacs.fold(seedRanges) { ranges, (almanacMaps) ->
            // For every almanac mapping for a given almanac,
            // * If source and range intersect
            //   * map the intersection to the destination, but
            //   * retain the difference
            // * Otherwise, retain the entire range
            // That is, don't try to check the intersection multiple times against ranges that have
            // already been mapped into the destination context. At the end, combine the unaccepted
            // and the accepted ranges to account for the transparent mapping into the destination
            // context when no source ranges are matched.
            almanacMaps.fold(
                Pair(ranges, emptyList<LongRange>())
            ) { (unaccepted, accepted), (source, offset) ->
                // This could be done with a .map, but it means we build a list of pairs of lists
                // of ranges which ends up being cumbersome to flatten out into a pair of lists.
                // Instead, just fold and manually build up the pair of list of ranges
                unaccepted.fold(Pair(emptyList(), accepted)) { (rejects, accepts), range ->
                    val intersection = range.intersect(source)
                    val (newRejects, newAccepts) =
                        if (intersection.isEmpty()) Pair(listOf(range), emptyList())
                        else Pair(
                            listOf(
                                range.first..<intersection.first,
                                intersection.last + 1..range.last
                            ).filter { !it.isEmpty() },
                            listOf(intersection.first + offset..intersection.last + offset)
                        )
                    Pair(newRejects + rejects, newAccepts + accepts)
                }
            }.let { (unacceptedRanges, acceptedRanges) ->
                acceptedRanges + unacceptedRanges
            }
        }

        return locationRanges.minOf { it.first }
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