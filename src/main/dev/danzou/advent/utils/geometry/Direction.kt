package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Pos

enum class Direction(val dir: Pos) {
    LEFT(Pos(-1, 0)),
    RIGHT(Pos(1, 0)),
    DOWN(Pos(0, -1)),
    UP(Pos(0, 1));
}