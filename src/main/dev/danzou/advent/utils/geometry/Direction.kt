package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Pos

enum class Direction(val dir: Pos) {
    LEFT(Pos(-1, 0)),
    RIGHT(Pos(1, 0)),
    DOWN(Pos(0, -1)),
    UP(Pos(0, 1));
}

enum class Compass(val dir: Pos) {
    NORTH(Pos(0, -1)),
    EAST(Pos(1, 0)),
    SOUTH(Pos(0, 1)),
    WEST(Pos(-1, 0)),
    NORTHEAST(Pos(1, -1)),
    SOUTHEAST(Pos(1, 1)),
    SOUTHWEST(Pos(-1, 1)),
    NORTHWEST(Pos(-1, -1));

    companion object {
        fun cardinalDirections(): Set<Compass> =
            setOf(NORTH, EAST, SOUTH, WEST)

        fun ordinalDirections(): Set<Compass> =
            setOf(NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST)

        fun directions(): Set<Compass> =
            values().toSet()
    }
}