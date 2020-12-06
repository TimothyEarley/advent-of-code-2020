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
