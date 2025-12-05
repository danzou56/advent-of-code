package dev.danzou.advent24

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day22 : AdventTestRunner24("Monkey Market") {

  override val timeout: Duration = Duration.ofSeconds(20)

  fun mix(premix: Long, secret: Long): Long = premix xor secret

  fun prune(mix: Long): Long = mix % 16777216L

  fun evolve(secret: Long): Long {
    val secret1 = prune(mix(secret * 64L, secret))
    val secret2 = prune(mix(secret1 / 32L, secret1))
    val secret3 = prune(mix(secret2 * 2048L, secret2))
    return secret3
  }

  fun prices(secret: Long, iters: Int): List<Long> {
    val secrets = mutableListOf<Long>()
    tailrec fun step(secret: Long, iters: Int): Long {
      secrets.add(secret)
      if (iters == 0) return secret
      return step(evolve(secret), iters - 1)
    }
    step(secret, iters)
    return secrets
  }

  override fun part1(input: String): Long {
    val secrets = input.lines().map { it.toLong() }
    return secrets.sumOf { prices(it, 2000).last() }
  }

  override fun part2(input: String): Long {
    return part2(input, 2000)
  }

  fun part2(input: String, evolutions: Int): Long {
    val secrets = input.lines().map(String::toLong)

    val prices = secrets.map { prices(it, evolutions) }
    val bananas = prices.map { it.map { it % 10 } }
    val changes = bananas.map { it.windowed(2, 1).map { (a, b) -> b to (b - a) } }
    val sequences: List<Map<List<Long>, Long>> =
        changes.map {
          it.windowed(4)
              .map { bananaSequences ->
                val (bananas, changes) = bananaSequences.unzip()
                changes to bananas.last()
              }
              .groupBy({ it.first }, { it.second })
              .mapValues { (_, v) -> v.first() }
        }.map { HashMap(it) }

    return sequences
        .flatMap { it.keys }
        .toSet()
        .maxOf { sequence ->
          sequences.sumOf { sequenceMap -> sequenceMap[sequence] ?: 0 }
        }
  }

  @Test
  fun testExample() {
    """
      1
      10
      100
      2024
    """
        .trimIndent()
        .let { input -> assertEquals(37327623L, part1(input)) }

    """
      1
      2
      3
      2024
    """
        .trimIndent()
        .let { input -> assertEquals(23, part2(input)) }
  }

  @Test
  fun testSmallExample() {
    """
      123
    """
        .trimIndent()
        .let { input -> assertEquals(6, part2(input, 9)) }
  }

  @Test
  fun testEvolve() {
    val secret = 123L
    assertEquals(15887950L, evolve(secret))
    assertEquals(16495136L, evolve(evolve(secret)))
  }
}
