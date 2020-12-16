package de.earley.adventofcode.day16

import de.earley.adventofcode.split

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val (specStrings, myTicketString, nearbyTicketsString) = input
        .split { it.isBlank() }

    val specs = specStrings.map {
        val (a, b) = it.substringAfter(": ").split(" or ", limit = 2)
        val rangeA = a.split('-', limit = 2).map(String::toInt).let { (i, j) -> i .. j }
        val rangeB = b.split('-', limit = 2).map(String::toInt).let { (i, j) -> i .. j }

        Spec(it.substringBefore(':'), rangeA, rangeB)
    }

    val myTicket : Ticket = myTicketString.drop(1).single().split(',').map(String::toInt)

    val tickets : List<Ticket> = nearbyTicketsString.drop(1).map { line ->
        line.split(',').map(String::toInt)
    }

    println("Part one: ${partOne(specs, tickets)}")
    println("Part two: ${partTwo(specs, tickets, myTicket)}")
}

private data class Spec(
    val name: String,
    val rangeA: IntRange,
    val rangeB: IntRange
) {
    fun match(i : Int) : Boolean = rangeA.contains(i) || rangeB.contains(i)
}

private typealias Ticket = List<Int>

private fun partOne(specs : List<Spec>, tickets : List<Ticket>) : Int
    = tickets.flatten().filter { i -> specs.none { it.match(i) } }.sum()


private fun partTwo(specs : List<Spec>, tickets : List<Ticket>, myTicket : Ticket) : Long {

    val validTickets = tickets.filter { ticket ->
        ticket.all { i -> specs.any { it.match(i)} }
    }

    val byPosition : Map<Int, List<Int>> = validTickets.flatMap { ticket ->
        ticket.withIndex()
    }.groupBy(IndexedValue<Int>::index, IndexedValue<Int>::value)

    val possibleSpecs = byPosition.mapValues { (_, numbers) ->
        specs.filter { spec -> numbers.all { spec.match(it)  } }
    }

    // sort to first look at the ones where only one spec fits
    val mapping = findCorrectCombination(possibleSpecs.entries.sortedBy { it.value.size })

    return mapping.entries
        .filter { (_, spec) -> spec.name.startsWith("departure") }
        .fold(1L) { acc, (i, _) ->
            acc * myTicket[i]
        }
}

private fun findCorrectCombination(
    possibleSpecs: List<Map.Entry<Int, List<Spec>>>,
    used : Set<Spec> = emptySet(),
) : Map<Int, Spec> {
    if (possibleSpecs.isEmpty()) return emptyMap()

    val (i, specs) = possibleSpecs.first()
    val okaySpecs = specs.filterNot { used.contains(it) }

    return if (okaySpecs.size == 1) {
        val spec = okaySpecs.single()
        findCorrectCombination(possibleSpecs.drop(1), used + spec) + (i to spec)
    } else {
        // this was not needed for the puzzle input
        // in this case we would have to go though all the candidates and find the
        // one that works. If none are left abandon this case
        TODO()
    }
}