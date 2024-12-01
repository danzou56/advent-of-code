package dev.danzou.advent.utils

import kotlin.math.sqrt

fun gcd(i1: Int, i2: Int): Int {
    var gcd = 1

    var i = 1
    while (i <= i1 && i <= i2) {
        // Checks if i is factor of both integers
        if (i1 % i == 0 && i2 % i == 0)
            gcd = i
        ++i
    }

    return gcd
}

fun gcd(l1: Long, l2: Long): Long {
    var gcd = 1L
    var i = 1L
    while (i <= l1 && i <= l2) {
        // Checks if i is factor of both integers
        if (l1 % i == 0L && l2 % i == 0L)
            gcd = i
        ++i
    }

    return gcd
}

fun lcm(i1: Int, i2: Int): Int = i1 * i2 / gcd(i1, i2)

fun lcm(l1: Long, l2: Long): Long = l1 * l2 / gcd(l1, l2)

fun Int.pow(i: Int): Int =
    generateSequence { this }.take(i).reduceOrNull(Int::times) ?: 1

fun Long.pow(i: Int): Long =
    generateSequence { this }.take(i).reduceOrNull(Long::times) ?: 1

fun Int.gaussianSum(): Int =
    this * (this + 1) / 2

fun Long.gaussianSum(): Long =
    this * (this + 1) / 2

val E = 1e-10

/**
 * Find solution(s) of the quadratic equation a * x^2 + b * x + c = 0 for x.
 * Results, if any, are returned in ascending order.
 */
fun quadSolve(a: Double, b: Double, c: Double): List<Double> {
    val discriminant = b * b - 4 * a * c
    return when {
        discriminant < -E -> emptyList()
        discriminant in -E..E -> listOf(-b / (2 * a))
        else -> listOf(-b + sqrt(discriminant), -b - sqrt(discriminant)).map { it / (2 * a) }.sorted()
    }
}

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