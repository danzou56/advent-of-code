package dev.danzou.advent.utils

import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText

object DotEnv {
    val backing: Map<String, String> = run {
        val path = Path.of(".env")
        if (path.notExists()) return@run emptyMap()
        val text = path.readText()
        text.split("\n").map {
            it.split(Regex("="), 2)
        }.associate { (k, v) -> k to v }
    }

    operator fun get(key: String): String? = backing[key] ?: System.getenv(key)
}