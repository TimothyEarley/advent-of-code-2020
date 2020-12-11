package de.earley.adventofcode.day11

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val wa = parseWaitingArea(input)


    println("Part one: ${partOne(wa)}")
    println("Part two: ${partTwo(wa)}")
}

private enum class Space {
    FLOOR, EMPTY_SEAT, OCCUPIED_SEAT
}

private data class WaitingArea(
    private val seats : List<List<Space>>
) {
    operator fun get(x: Int, y: Int): Space? = seats.getOrNull(y)?.getOrNull(x)

    fun map(f: (x: Int, y: Int, s: Space) -> Space): WaitingArea {
        return WaitingArea(seats.mapIndexed { y, l ->
            l.mapIndexed { x, s ->
                f(x, y, s)
            }
        })
    }

    fun count(p : (Space) -> Boolean) : Int = seats.sumOf {
        it.count(p)
    }

    fun neighbours(x : Int, y : Int) : List<Space?> {
        return (-1..1).flatMap { dx ->
            (-1..1)
                .filter { dy -> dx != 0 || dy != 0 }
                .map { dy -> this[x + dx, y + dy]
            }
        }
    }

    override fun toString() = seats.joinToString("\n") { line ->
        line.joinToString("") {
            when (it) {
                Space.FLOOR -> "."
                Space.EMPTY_SEAT -> "L"
                Space.OCCUPIED_SEAT -> "#"
            }
        }
    }
}

private fun parseWaitingArea(lines : List<String>) =
    WaitingArea(
        lines.map { line ->
            line.map { when (it) {
                '.' -> Space.FLOOR
                'L' -> Space.EMPTY_SEAT
                '#' -> Space.OCCUPIED_SEAT
                else -> throw Exception("Could not parse $line, unknown char '$it'")
            } }
        }
    )

/**
 * Rules:
 * - empty seats become occupied if no seats adjacent are occupied
 * - occupied seats become empty if [leaveSeatThreshold] or more adjacent seats are also occupied
 *
 * [adjacent] is the rule which seats are considered adjacent
 *
 * The boolean is true if a change occurred
 */
private fun WaitingArea.step(leaveSeatThreshold: Int, adjacent : WaitingArea.(Int, Int) -> List<Space?>): Pair<Boolean, WaitingArea> {
    var change = false
    val result = map { x, y, s ->
        when (s) {
            Space.FLOOR -> Space.FLOOR
            Space.EMPTY_SEAT ->
                if (adjacent(x, y).count { it == Space.OCCUPIED_SEAT } == 0) {
                    change = true
                    Space.OCCUPIED_SEAT
                } else Space.EMPTY_SEAT
            Space.OCCUPIED_SEAT ->
                if (adjacent(x, y).count { it == Space.OCCUPIED_SEAT} >= leaveSeatThreshold) {
                    change = true
                    Space.EMPTY_SEAT
                } else Space.OCCUPIED_SEAT
        }
    }
    return change to result
}

private fun partOne(wa : WaitingArea) : Int {
    var state = true to wa
    while (state.first) {
        state = state.second.step(4) { x, y -> neighbours(x, y)}
    }

    return state.second.count { it == Space.OCCUPIED_SEAT }
}

private fun partTwo(wa : WaitingArea) : Int {
    var state = true to wa
    while (state.first) {
        state = state.second.step(5) { x, y ->
            // cast rays in the eight directions
            (-1 .. 1).flatMap { dx ->
                (-1 .. 1).filter { dy -> dx != 0 || dy != 0 }
                    .map { dy ->
                        // find the first visible chair, i.e. the first non floor
                        var tx = x + dx
                        var ty = y + dy
                        while (this[tx, ty] == Space.FLOOR) {
                            tx += dx
                            ty += dy
                        }
                        this[tx, ty]
                    }
        }
        }
    }

    return state.second.count { it == Space.OCCUPIED_SEAT }
}