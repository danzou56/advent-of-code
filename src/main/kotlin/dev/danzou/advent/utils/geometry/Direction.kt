package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Pos

enum class Direction(val dir: Pos) {
    LEFT(Pos(-1, 0)),
    RIGHT(Pos(1, 0)),
    DOWN(Pos(0, -1)),
    UP(Pos(0, 1));

    val invDir
        get() = Pos(dir.first, dir.second * -1)
}

enum class Compass(val dir: Pos) {
    NORTH(Pos(0, -1)),
    EAST(Pos(1, 0)),
    SOUTH(Pos(0, 1)),
    WEST(Pos(-1, 0)),
    NORTHEAST(Pos(1, -1)),
    SOUTHEAST(Pos(1, 1)),
    SOUTHWEST(Pos(-1, 1)),
    NORTHWEST(Pos(-1, -1)),
    CENTER(Pos(0, 0));

    companion object {
        val CARDINAL: Set<Compass> = setOf(NORTH, EAST, SOUTH, WEST)
        val CARDINAL_DIRECTIONS: Set<Pos> = CARDINAL.map(Compass::dir).toSet()
        val ORDINAL: Set<Compass> = setOf(NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST)
        val ORDINAL_DIRECTIONS: Set<Pos> = ORDINAL.map(Compass::dir).toSet()
        val ALL: Set<Compass> = CARDINAL + ORDINAL
        val ALL_DIRECTIONS: Set<Pos> = CARDINAL_DIRECTIONS + ORDINAL_DIRECTIONS

        fun fromDir(dir: Pos): Compass {
            return when (dir) {
                Pos(0, -1) -> NORTH
                Pos(1, 0) -> EAST
                Pos(0, 1) -> SOUTH
                Pos(-1, 0) -> WEST
                else -> TODO()
            }
        }
    }
}