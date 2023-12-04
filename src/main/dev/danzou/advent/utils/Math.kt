package dev.danzou.advent.utils


fun Int.pow(i: Int): Int =
    generateSequence { this }.take(i).reduceOrNull(Int::times) ?: 1

fun Long.pow(i: Int): Long =
    generateSequence { this }.take(i).reduceOrNull(Long::times) ?: 1

fun Int.gaussianSum(): Int =
    this * (this + 1) / 2

fun Long.gaussianSum(): Long =
    this * (this + 1) / 2

fun Int.factorial(knowing: Int = 0, hasFactorial: Int = 1): Int {
    require(this >= knowing)

    fun factorial(num: Int): Int {
        if (num == knowing) return hasFactorial
        return num * factorial(num - 1)
    }
    return factorial(this)
}

val Int.`!`
    get(): Int = this.factorial()

fun Int.`!`(knowing: Int = 0, hasFactorial: Int = 1): Int =
    this.factorial(knowing, hasFactorial)

fun Long.factorial(knowing: Long = 0, hasFactorial: Long = 1): Long {
    require(this >= knowing)

    fun factorial(num: Long): Long {
        if (num == knowing) return hasFactorial
        return num * factorial(num - 1)
    }

    return factorial(this)
}

val Long.`!`
    get(): Long = this.factorial()

fun Long.`!`(knowing: Long = 0, hasFactorial: Long = 1): Long =
    this.factorial(knowing, hasFactorial)

infix fun Int.choose(k: Int): Int {
    require(this >= k)
    require(k >= 0)

    val `n-k` = this - k
    val `k!`: Int
    val `(n-k)!`: Int
    val `n!`: Int
    if (`n-k` <= k) {
        `(n-k)!` = `n-k`.`!`
        `k!` = `n-k`.`!`(knowing = `n-k`, hasFactorial = `(n-k)!`)
        `n!` = this.`!`(knowing = `k`, hasFactorial = `k!`)
    } else {
        `k!` = `k`.`!`
        `(n-k)!` = `n-k`.`!`(knowing = `k`, hasFactorial = `k!`)
        `n!` = this.`!`(knowing = `n-k`, hasFactorial = `(n-k)!`)
    }
    return `n!` / (`k!` * `(n-k)!`)
}

infix fun Long.choose(k: Long): Long {
    require(this >= k)
    require(k >= 0)

    val `n-k` = this - k
    val `k!`: Long
    val `(n-k)!`: Long
    val `n!`: Long
    if (`n-k` <= k) {
        `(n-k)!` = `n-k`.`!`
        `k!` = `n-k`.`!`(knowing = `n-k`, hasFactorial = `(n-k)!`)
        `n!` = this.`!`(knowing = `k`, hasFactorial = `k!`)
    } else {
        `k!` = `k`.`!`
        `(n-k)!` = `n-k`.`!`(knowing = `k`, hasFactorial = `k!`)
        `n!` = this.`!`(knowing = `n-k`, hasFactorial = `(n-k)!`)
    }
    return `n!` / (`k!` * `(n-k)!`)
}