import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

private const val BASE_PATH = "inputs"
private const val FILE_PREFIX = "day"
private const val FILE_SUFFIX = ".in"

/**
 * Reads lines from the given input txt file.
 */
fun readInput() = readInput("${BASE_PATH.removeSuffix("/")}/" +
        "$FILE_PREFIX${getExecutingDayNumber()}$FILE_SUFFIX")

fun readInput(name: String) = File(name).readLines()

/**
 *
 */
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

