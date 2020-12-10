package de.earley.adventofcode

/**
 * Split a list into lists of lists indicated by the [splitOn] predicate.
 * The item split on is not in the output.
 *
 * Example: listOf(1, 2, 3, 0, 5).split { it == 0 } == listOf(listOf(1, 2, 3), listOf(5))
 */
fun <A> List<A>.split(splitOn : (A) -> Boolean) : List<List<A>> = fold(emptyList<List<A>>() to emptyList<A>()) { (acc, curAcc), cur ->
    if (splitOn(cur)) {
        (acc.addElement(curAcc)) to emptyList()
    } else {
        acc to (curAcc + cur)
    }
}.let { (a, b) -> a.addElement(b) }

// see https://youtrack.jetbrains.com/issue/KT-9992 why a + b does not work
private fun <A> List<List<A>>.addElement(l : List<A>) : List<List<A>> = toMutableList().apply { add(l) }


/**
 * The list needs to be sorted.
 * Starts at the first (smallest) and last (highest) element.
 * If that sum is too low it can only be increased by increasing the smaller index.
 * If it is too high decrement the high index.
 */
fun <N : Number> List<N>.findSumToOrNull(sum: N): Pair<N, N>? {
    var low = 0
    var high = lastIndex

    while (low < high) {
        // try this sum
        val l = get(low)
        val h = get(high)
        val thisSum : N = l + h
        when {
            thisSum < sum -> {
                // need to be bigger
                low++
            }
            thisSum == sum -> return l to h
            thisSum > sum -> {
                // need to be smaller
                high--
            }
        }
    }

    // we have not found any match
    return null
}

@Suppress("UNCHECKED_CAST") // check by when
operator fun <N : Number> N.plus(other: N): N {
    return when (this) {
        is Long   -> (this.toLong() + other.toLong()) as N
        is Int    -> (this.toInt()  + other.toInt()) as N
        is Short  -> (this.toShort() + other.toShort()) as N
        is Byte   -> (this.toByte() + other.toByte()) as N
        is Double -> (this.toDouble() + other.toDouble()) as N
        is Float  -> (this.toFloat() + other.toFloat()) as N
        else      -> throw RuntimeException("Unknown numeric type")
    }
}

operator fun <N : Number> N.compareTo(other: N): Int {
    return when (this) {
        is Long   -> this.toLong().compareTo(other.toLong())
        is Int    -> this.toInt().compareTo(other.toInt())
        is Short  -> this.toShort().compareTo(other.toShort())
        is Byte   -> this.toByte().compareTo(other.toByte())
        is Double -> this.toDouble().compareTo(other.toDouble())
        is Float  -> this.toFloat().compareTo(other.toFloat())
        else      -> throw RuntimeException("Unknown numeric type")
    }
}

fun <T> Iterable<T>.sumByLong(f: (T) -> Long): Long {
    var sum : Long = 0
    for (e in this) {
        sum += f(e)
    }
    return sum
}