package de.earley.adventofcode.day13

import java.math.BigInteger


fun main() {
    val lines = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val (earliestDepartureString, busTimes) = lines

    val earliestDeparture = earliestDepartureString.toInt()

    val bussesInService = busTimes.split(',')
        .mapNotNull { it.toIntOrNull() }

    val busConstraints = busTimes.split(',')
        .map {
            when (it) {
                "x" -> Constraint.NoConstraint
                else -> Constraint.IdConstraint(it.toInt())
            }
        }

    println("Part one: ${partOne(earliestDeparture, bussesInService)}")
    println("Part one: ${partTwo(busConstraints)}")
}

/**
 * Find the first bus that we can catch
 * Busses leave at an increment of their id
 */
private fun partOne(earliestDeparture: Int, bussesInService: List<Int>): Int {

    val (id, wait) = bussesInService
        .map { id ->
            // find lowest multiple higher than earliest departure
            val diff = earliestDeparture.rem(id)
            val wait = id - diff
            id to wait
        }.minByOrNull { it.second } ?: throw Exception("No solution found")

    return id * wait
}

private sealed class Constraint {
    object NoConstraint : Constraint()
    data class IdConstraint(val id: Int) : Constraint()
}


/**
 * Find time t such that bus one arrives at t, bus two arrives at t+1, etc.
 */
private fun partTwo(busTimes: List<Constraint>): Long {

    val idOffsets = busTimes.mapIndexedNotNull { index, constraint ->
        when (constraint) {
            Constraint.NoConstraint -> null
            is Constraint.IdConstraint -> (index to constraint.id)
        }
    }.sortedBy { - it.first }

    // start time to step size
    val result = idOffsets.fold(0L to 1L) { acc, p ->
        val (start, stepSize) = acc
        val (offset, bus) = p
        var x = start
        while ((x + offset) % bus != 0L) {
            x += stepSize
        }
        x to (stepSize * bus)
    }

    return result.first
}