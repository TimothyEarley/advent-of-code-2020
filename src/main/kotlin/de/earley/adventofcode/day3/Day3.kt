package de.earley.adventofcode.day3

fun main() {

    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val map = parseInput(input)

    println(map.treesOnSlope(3, 1))

    fun part2(dir : Pair<Int, Int>) : Int = map.treesOnSlope(dir.first, dir.second)
    println(
        listOf(1 to 1, 3 to 1, 5 to 1, 7 to 1, 1 to 2).map(::part2).fold(1,Int::times)
    )

}

private enum class Tile {
    OPEN, TREE
}
private fun Tile?.treeCount() : Int = when (this) {
    Tile.OPEN, null -> 0
    Tile.TREE -> 1
}

private fun parseInput(lines : List<String>) = Map(
    lines.map { line ->
        line.toCharArray().map { when (it) {
            '.' -> Tile.OPEN
            '#' -> Tile.TREE
            else -> throw Exception("Failed to parse char $it in line $line")
        } }
    }
)


private class Map(
    private val data : List<List<Tile>>
) {
    private val width = data[0].size
    val height = data.size

    operator fun get(x : Int, y : Int) : Tile? = data.getOrNull(y)?.get(x.rem(width))
}


private fun Map.treesOnSlope(right : Int, down : Int) : Int {
    tailrec fun rec(x : Int, y : Int, acc : Int) : Int =
        if (y >= height) acc
        else rec(x + right, y + down, acc + this[x, y].treeCount())
    return rec(0, 0, 0)
}