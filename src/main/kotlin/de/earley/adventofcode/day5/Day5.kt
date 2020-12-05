package de.earley.adventofcode.day5

fun main() {
    val seats = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(::parseSeat)

    val maxId = seats.maxOf(Seat::id)

    println("Part one: $maxId")

    val mySeatId = seats.map(Seat::id)
        .sorted()
        .zipWithNext()
        // find the first seat where the next id is not taken
        .find { (cur, next) -> cur + 1 != next }!!
        .first + 1

    println("Part Two: $mySeatId")

}

private data class Seat(val row : Int, val col : Int) {
    val id : Int get() = row * 8 + col
}

private fun parseSeat(line : String) : Seat {
    val (rowString, colString) = line.chunked(7)
    val row = rowString.findNumber(0..127, 'F', 'B')
    val col = colString.findNumber(0..7, 'L', 'R')
    return Seat(row, col)
}

private fun String.findNumber(range: IntRange, lower : Char, higher : Char) : Int
    = fold(range) { r, c ->
    val diff = (r.last - r.first) + 1 // +1 for rounding
    when (c) {
        lower -> r.first .. (r.last - diff/2)
        higher -> (r.first + diff/2) .. r.last
        else -> throw Exception("Could not parse $this: Unknown char $c")
    }
}.single()