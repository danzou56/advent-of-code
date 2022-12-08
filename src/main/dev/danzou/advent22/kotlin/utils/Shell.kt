package dev.danzou.advent22.kotlin.utils

/**
 * A representation of a command executed in a shell
 */
data class Process(val command: Command, val output: Output) {

}

fun String.toProcess(): Process {
    throw NotImplementedError()
}

enum class Cmd(command: String) {
    ;
    companion object {
        fun fromString(command: String) =
            values().associateBy { it: Cmd -> it.name }[command]!!
    }
}

fun String.toCommand(): Command {
    throw NotImplementedError()
}

data class Command(val command: Cmd, val args: List<String>) {
    constructor(vararg args: String) : this(Cmd.fromString(args.first()), args.toList())
}

data class Output(val output: String)