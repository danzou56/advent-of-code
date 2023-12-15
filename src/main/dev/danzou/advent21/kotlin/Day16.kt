package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21

typealias Binary = List<Int>
typealias ParseResult<T> = Pair<Binary, T>

internal class Day16 : AdventTestRunner21("Packet Decoder") {

    fun getBinary(input: String): Binary =
        input.flatMap {
            it.digitToInt(16)
                .toUInt()
                .toString(radix = 2)
                .padStart(4, '0')
                .map { it.digitToInt() }
        }

    override fun part1(input: String): Int {
        val (_, packet) = consumePacket(getBinary(input))

        fun sumVersions(packet: Packet): Int =
            packet.version + when(packet) {
                is LiteralPacket -> 0
                is OperatorPacket -> packet.subpackets.sumOf { sumVersions(it) }
            }

        return sumVersions(packet)
    }

    override fun part2(input: String): Long {
        val (_, packet) = consumePacket(getBinary(input))
        return interpretPacket(packet)
    }

    enum class Type(val operation: Int) {
        SUM(0),
        PROD(1),
        MIN(2),
        MAX(3),
        LIT(4),
        GT(5),
        LT(6),
        EQ(7);

        companion object {
            private val fromIntLookup = Type.values().associateBy({ it.operation }, { it })
            fun fromInt(operation: Int) = fromIntLookup[operation] ?: throw IllegalArgumentException()

        }
    }

    sealed class Packet(val version: Int)
    class LiteralPacket(version: Int, val data: Long) : Packet(version)
    class OperatorPacket(version: Int, val operation: Type, val subpackets: List<Packet>) : Packet(version)
    class ParseException(message: String? = null) : IllegalArgumentException(message)

    fun buildInt(binary: Binary, digits: Int): ParseResult<Int> =
        Pair(
            binary.drop(digits),
            binary.slice(0 until digits).joinToString("") { it.toString() }.toInt(radix = 2)
        )

    fun buildLong(binary: Binary, digits: Int): ParseResult<Long> =
        Pair(
            binary.drop(digits),
            binary.slice(0 until digits).joinToString("") { it.toString() }.toLong(radix = 2)
        )

    fun consumeVersion(binary: Binary): ParseResult<Int> =
        buildInt(binary, 3)

    fun consumeType(binary: Binary): ParseResult<Type> {
        val (rest, type) = buildInt(binary, 3)
        return ParseResult(
            rest,
            Type.fromInt(type)
        )
    }

    // This should be tailrec
    fun consumeLiteralData(binary: Binary, num: Long = 0): ParseResult<Long> {
        val (rest, label) = buildInt(binary, 1)
        return when (label) {
            0 -> buildLong(rest, 4).let { (rest, l) ->
                Pair(rest, (num shl 4) + l)
            }

            1 -> buildLong(rest, 4).let { (rest, l) ->
                consumeLiteralData(rest, (num shl 4) + l)
            }

            else -> throw ParseException()
        }
    }

    fun consumeOp(binary: Binary): ParseResult<List<Packet>> {
        val (rest, type) = buildInt(binary, 1)
        return when (type) {
            0 -> consumeOpLenData(rest)
            1 -> consumeOpNumData(rest)
            else -> throw ParseException()
        }
    }

    fun consumeOpLenData(binary: Binary): ParseResult<List<Packet>> {
        val LEN_FIELD_SIZE = 15
        var (subpacketsBinary, len) = buildInt(binary, LEN_FIELD_SIZE)
        val subpackets = mutableListOf<Packet>()
        while (true) {
            val (rest, packet) = consumePacket(subpacketsBinary)
            subpacketsBinary = rest
            subpackets += packet
            if (binary.size - LEN_FIELD_SIZE - rest.size == len) return ParseResult(rest, subpackets)
            if (binary.size - LEN_FIELD_SIZE - rest.size > len) throw ParseException()
        }
    }

    fun consumeOpNumData(binary: Binary): ParseResult<List<Packet>> {
        val COUNT_FIELD_SIZE = 11
        var (subpacketsBinary, count) = buildInt(binary, COUNT_FIELD_SIZE)
        val subpackets = mutableListOf<Packet>()
        while (true) {
            val (rest, packet) = consumePacket(subpacketsBinary)
            subpacketsBinary = rest
            subpackets += packet
            if (subpackets.size == count) return ParseResult(rest, subpackets)
        }
    }

    fun consumePacket(binary: Binary): ParseResult<Packet> =
        consumeVersion(binary).let { (rest, version) ->
            consumeType(rest).let { (rest, type) ->
                when (type) {
                    Type.LIT -> consumeLiteralData(rest).let { (rest, data) ->
                        ParseResult(rest, LiteralPacket(version, data))
                    }

                    else -> consumeOp(rest).let { (rest, subpackets) ->
                        ParseResult(rest, OperatorPacket(version, type, subpackets))
                    }
                }
            }
        }

    fun interpretPacket(packet: Packet): Long =
        when (packet) {
            is LiteralPacket -> packet.data
            is OperatorPacket -> packet.subpackets.map(::interpretPacket).let { subpackets ->
                when (packet.operation) {
                    Type.SUM -> subpackets.sum()
                    Type.PROD -> subpackets.reduce(Long::times)
                    Type.MIN -> subpackets.min()
                    Type.MAX -> subpackets.max()
                    Type.GT -> subpackets.let { it[0] > it[1] }
                    Type.LT -> subpackets.let { it[0] < it[1] }
                    Type.EQ -> subpackets.let { it[0] == it[1] }
                    else -> throw IllegalStateException()
                }
            }.let {
                when (it) {
                    is Boolean -> if (it) 1 else 0
                    is Long -> it
                    else -> throw IllegalStateException()
                }

            }
        }
}