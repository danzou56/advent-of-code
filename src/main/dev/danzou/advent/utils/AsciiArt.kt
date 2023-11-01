package dev.danzou.advent.utils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AsciiArt(
    val art: String,
    val format: AsciiArtFormat = AsciiArtFormat.F4_6
) {
    val text: String
        get() = art.split("\n")
            .map { row -> row.chunked(format.width + 1) { it.take(format.width) } }
            .transpose()
            .map {
                format.artToChar.getValue(
                    it.joinToString("\n")
                ) ?: throw IllegalArgumentException("Invalid art")
            }
            .joinToString("")
    val isText: Boolean
        get() = try {
            text != null
        } catch (_: IllegalArgumentException) {
            false
        }

    override fun toString(): String = "$art${if (isText) "\n$text" else ""}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AsciiArt

        if (art != other.art) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = art.hashCode()
        result = 31 * result + format.hashCode()
        return result
    }

    companion object {
        fun fromText(text: String, format: AsciiArtFormat = AsciiArtFormat.F4_6): AsciiArt =
            throw NotImplementedError("NIE")

        fun fromOccupied(occupied: List<Pos>, format: AsciiArtFormat = AsciiArtFormat.F4_6): AsciiArt =
            AsciiArt(
                (0..occupied.maxOf { it.y }).map { y ->
                    (0..occupied.maxOf { it.x }).map { x ->
                        if (Pos(x, y) in occupied) format.occupiedChar
                        else format.emptyChar
                    }.joinToString("")
                }.joinToString("\n"),
                format
            )

    }

}


sealed class AsciiArtFormat(
    internal val artToChar: Map<String, Char?>,
//    internal val charToArt: Map<Char, String?>,
    private val format: Format,
    val occupiedChar: Char,
    val emptyChar: Char,
) {
    val width: Int
        get() = format.width
    val height: Int
        get() = format.height

    enum class Format(val width: Int, val height: Int) {
        F4_6(4, 6),
        F6_10(6, 10)
    }

    constructor(
        letters: List<Char>,
        art: String,
        format: Format,
        occupiedChar: Char,
        emptyChar: Char,
        default: Char? = null
    ) : this(
        art.split("\n")
            .map { row -> row.chunked(format.width + 1) { it.take(format.width) } }
            .transpose()
            .map { it.joinToString("\n") }
            .zip(letters)
            .toMap()
            .withDefault { default },
        format,
        occupiedChar,
        emptyChar
    )

    companion object {
        const val DEFAULT_EMPTY = '.'
        const val DEFAULT_OCCUPIED = '#'

        fun F4_6(
            occupiedChar: Char = DEFAULT_OCCUPIED,
            emptyChar: Char = DEFAULT_EMPTY
        ): AsciiArtFormat4_6 = AsciiArtFormat4_6(occupiedChar, emptyChar)

        fun F6_10(
            occupiedChar: Char = DEFAULT_OCCUPIED,
            emptyChar: Char = DEFAULT_EMPTY
        ): AsciiArtFormat6_10 = AsciiArtFormat6_10(occupiedChar, emptyChar)

        val F4_6 = AsciiArtFormat4_6()
        val F6_10 = AsciiArtFormat6_10()
    }

    class AsciiArtFormat4_6(
        occupiedChar: Char = DEFAULT_OCCUPIED,
        emptyChar: Char = DEFAULT_EMPTY
    ) : AsciiArtFormat(
        "ABCEFGHIJKLOPRSUZ".toList(),
        """
            .##..###...##..####.####..##..#..#.###....##.#..#.#.....##..###..###...###.#..#.####
            #..#.#..#.#..#.#....#....#..#.#..#..#......#.#.#..#....#..#.#..#.#..#.#....#..#....#
            #..#.###..#....###..###..#....####..#......#.##...#....#..#.#..#.#..#.#....#..#...#.
            ####.#..#.#....#....#....#.##.#..#..#......#.#.#..#....#..#.###..###...##..#..#..#..
            #..#.#..#.#..#.#....#....#..#.#..#..#...#..#.#.#..#....#..#.#....#.#.....#.#..#.#...
            #..#.###...##..####.#.....###.#..#.###...##..#..#.####..##..#....#..#.###...##..####
        """.trimIndent()
            .map {
                when (it) {
                    DEFAULT_OCCUPIED -> occupiedChar
                    DEFAULT_EMPTY -> emptyChar
                    else -> it
                }
            }.joinToString(""),
        Format.F4_6,
        occupiedChar,
        emptyChar
    )

    class AsciiArtFormat6_10(
        occupiedChar: Char = DEFAULT_OCCUPIED,
        emptyChar: Char = DEFAULT_EMPTY
    ) : AsciiArtFormat(
        "ABCEFGHJKLNPRXZ".toList(),
        """
            ..##...#####...####..######.######..####..#....#....###.#....#.#......#....#.#####..#####..#....#.######
            .#..#..#....#.#....#.#......#......#....#.#....#.....#..#...#..#......##...#.#....#.#....#.#....#......#
            #....#.#....#.#......#......#......#......#....#.....#..#..#...#......##...#.#....#.#....#..#..#.......#
            #....#.#....#.#......#......#......#......#....#.....#..#.#....#......#.#..#.#....#.#....#..#..#......#.
            #....#.#####..#......#####..#####..#......######.....#..##.....#......#.#..#.#####..#####....##......#..
            ######.#....#.#......#......#......#..###.#....#.....#..##.....#......#..#.#.#......#..#.....##.....#...
            #....#.#....#.#......#......#......#....#.#....#.....#..#.#....#......#..#.#.#......#...#...#..#...#....
            #....#.#....#.#......#......#......#....#.#....#.#...#..#..#...#......#...##.#......#...#...#..#..#.....
            #....#.#....#.#....#.#......#......#...##.#....#.#...#..#...#..#......#...##.#......#....#.#....#.#.....
            #....#.#####...####..######.#.......###.#.#....#..###...#....#.######.#....#.#......#....#.#....#.######
        """.trimIndent()
            .map {
                when (it) {
                    DEFAULT_OCCUPIED -> occupiedChar
                    DEFAULT_EMPTY -> emptyChar
                    else -> it
                }
            }.joinToString(""),
        Format.F6_10,
        occupiedChar,
        emptyChar
    )
}

class TestAsciiArtFormat {
    @Test
    fun testLetterForms() {
        val art = AsciiArt(
            """
                ####
                #...
                ###.
                #...
                #...
                ####
            """.trimIndent()
        )

        assertEquals("E", art.text)
//        assertTrue(art == "E")
    }
}