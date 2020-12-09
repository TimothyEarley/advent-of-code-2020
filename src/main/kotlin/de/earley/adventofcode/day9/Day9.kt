package de.earley.adventofcode.day9

import de.earley.adventofcode.findSumToOrNull

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .lineSequence()
        .map(String::toLong)
        .toList()

    val windowSize = 25 // 5 for test

    val wrongNumber = partOne(input, windowSize)
    println("Part one: $wrongNumber")
    println("Part two: ${partTwo(input, wrongNumber)}")

}

// find the first number that is not a sum of the previous [window]
// to speedup, keep the sort and do an insertion every iteration
private fun partOne(input : List<Long>, window : Int) : Long = input.windowed(window + 1) {
    // == null is true if there are no two that sum up to the target
    it.last() to (it.dropLast(1).sorted().findSumToOrNull(it.last()) == null)
}.find(Pair<Long, Boolean>::second)!!.first // !! if none found no solution!

private fun partTwo(input : List<Long>, target : Long) : Long {

    // find a continuous range in the input that sums to the target

    var range = 0 .. 1 // start with at least 2 numbers
    var sum = input[0] + input[1]

    while (sum != target) {

        when {
            sum < target -> {
                // too small, so add the next number
                val added = range.last + 1
                range = range.first .. added
                sum += input[added]
            }
            sum > target -> {
                // too big, remove the first number
                val removed = range.first
                range = (removed + 1) .. range.last
                sum -= input[removed]
            }
        }

        require(range.count() >= 2)
    }

    val inRange = range.map { input[it] }
    return inRange.minOrNull()!! + inRange.maxOrNull()!!
}