import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

private const val BASE_PATH = "inputs"
private const val FILE_PREFIX = "day"
private const val FILE_SUFFIX = ".in"

private const val RELOCATED_UTILS = "Use dev.danzou.utils.Utils instead"

/**
 * Reads lines from the given input txt file.
 */
@Deprecated(RELOCATED_UTILS)
fun readInput(name: String) = File(name).readLines()

@Deprecated(RELOCATED_UTILS)
fun readInput() = readInput("${BASE_PATH.removeSuffix("/")}/" +
        "$FILE_PREFIX${getExecutingDayNumber()}$FILE_SUFFIX")

@Deprecated(RELOCATED_UTILS)
fun getExecutingDayNumber(): Int {
    try {
        throw Exception()
    } catch (e: Exception) {
        return e.stackTrace.first {
            it.fileName?.startsWith("day", ignoreCase = true) ?: throw Exception(
                "`StackElement!` had no filename"
            )
        }.fileName!!.let {
            it.substring(3, it.indexOf('.'))
        }.toInt()
    }
}

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

typealias Matrix<T> = List<List<T>>
fun <T> Matrix<T>.getCellNeighbors(i: Int, j: Int): List<T> = listOfNotNull(
    this.getOrNull(i - 1)?.get(j),
    this.getOrNull(i + 1)?.get(j),
    this.get(i).getOrNull(j - 1),
    this.get(i).getOrNull(j + 1),
)

fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2!")
    }
    return Pair(this[0], this[1])
}