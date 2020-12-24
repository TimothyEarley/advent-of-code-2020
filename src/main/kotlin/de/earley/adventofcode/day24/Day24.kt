package de.earley.adventofcode.day24

import de.earley.adventofcode.parser.*
import java.lang.Exception

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(Grammar.line::deriveAll)
        .map {
            when (it) {
                is ParseResult.Ok -> it.set().single()
                is ParseResult.Error -> throw Exception(it.error.toString())
            }
        }

    println("Part one: ${partOne(input)}")
    println("Part two: ${partTwo(input)}")
}

private enum class Direction {
    EAST, SOUTHEAST, SOUTHWEST,
    WEST, NORTHWEST, NORTHEAST
}
private typealias Position = List<Direction>

private object Grammar {
    private val direction : Parser<Char, Direction> =
        string("se").map { Direction.SOUTHEAST } or
        string("sw").map { Direction.SOUTHWEST } or
        string("ne").map { Direction.NORTHEAST } or
        string("nw").map { Direction.NORTHWEST } or
        string("e").map { Direction.EAST } or
        string("w").map { Direction.WEST }

    val line : Parser<Char, List<Direction>> = many(direction)
}


private fun partOne(directions : List<Position>) : Int {
    return blackTiles(directions)
        .count()
}

private fun blackTiles(directions: List<Position>): List<Coordinates> =
    directions.map(::canonical)
        .groupingBy { it }
        .eachCount()
        .filter { it.value % 2 == 1 }
        .map { it.key }

// only two axis to have unique value
private data class Coordinates(
    val east : Int,
    val northWest : Int
)

// to unique representation
private fun canonical(direction: Position) : Coordinates {
    val grouped = direction.groupingBy { it }.eachCount().withDefault { 0 }
    // for each northwest we need to subtract one east and add one northEast
    // for each southeast we need to add one east and subtract one northEast
    return Coordinates(
        east = grouped.getValue(Direction.EAST) -
                grouped.getValue(Direction.WEST) +
                grouped.getValue(Direction.NORTHEAST) -
                grouped.getValue(Direction.SOUTHWEST),
        northWest = grouped.getValue(Direction.NORTHWEST) -
                grouped.getValue(Direction.SOUTHEAST) +
                grouped.getValue(Direction.NORTHEAST) -
                grouped.getValue(Direction.SOUTHWEST),
    )
}

private fun partTwo(positions: List<Position>) : Int {
    val init = blackTiles(positions).toSet()

    val last = (1..100).fold(init) { black, _ ->
        val new = black.toMutableList()

        new.retainAll { pos -> blackNeighbours(pos, black).let {
            ! ( it == 0 || it > 2)
        }}

        new.addAll(
            black.flatMap(::neighbours).toSet().filter { blackNeighbours(it, black) == 2 }
        )

        new.toSet()
    }

    return last.size
}

private fun blackNeighbours(pos : Coordinates, black : Set<Coordinates>) : Int =
    neighbours(pos).count(black::contains)

private fun neighbours(pos : Coordinates) : List<Coordinates> = listOf(
    pos.copy(east = pos.east + 1),
    pos.copy(east = pos.east - 1),
    pos.copy(northWest = pos.northWest + 1),
    pos.copy(northWest = pos.northWest - 1),
    pos.copy(east = pos.east + 1, northWest = pos.northWest + 1),
    pos.copy(east = pos.east - 1, northWest = pos.northWest - 1),
)