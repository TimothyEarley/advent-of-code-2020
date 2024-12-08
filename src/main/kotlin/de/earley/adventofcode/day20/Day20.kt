package de.earley.adventofcode.day20

import de.earley.adventofcode.split
import kotlin.collections.map
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun main() {
    val input = object {}.javaClass.getResourceAsStream("test.txt").bufferedReader()
        .readLines()

    val imageBlocks = input.split(String::isBlank)
    val regex = Regex("Tile (\\d+):")
    val images = imageBlocks.map {
        val (id) = regex.matchEntire(it.first())!!.destructured
        val data = it.drop(1).map { it.toList() }
        Image(id.toInt(), Grid(data.first().size, data.size, data.flatten()))
    }

    val assembled = assembleImage(images)

    println("Part one: ${partOne(assembled)}")
    println("Part two: ${partTwo(assembled, """
        |                  # 
        |#    ##    ##    ###
        | #  #  #  #  #  #   
    """.trimMargin())}")
}

private typealias ImageData = Grid<Char>

private data class Image(
    val id: Int,
    val image: ImageData
)

private data class TransformedImage(
    val id: Int,
    val original: Image,
    val size: Int,
    val flipped: Boolean,
    val rotation: Int,
    val topEdge: Int,
    val rightEdge: Int,
    val bottomEdge: Int,
    val leftEdge: Int
) {
    fun allPossible(): Sequence<TransformedImage> {
        return sequenceOf(
            this,
            this.rotate(1),
            this.rotate(2),
            this.rotate(3),
            this.flip(),
            this.flip().rotate(1),
            this.flip().rotate(2),
            this.flip().rotate(3),
        )
    }
}

private fun Image.toTransformed(): TransformedImage = TransformedImage(
    id,
    this,
    image.width,
    false,
    0,
    image.topRow().toInt(),
    image.rightCol().toInt(),
    image.botRow().toInt(),
    image.leftCol().toInt()
)

private fun List<Char>.toInt(): Int = joinToString("") {
    when (it) {
        '#' -> "1"
        '.' -> "0"
        else -> throw Exception("Corrupted image: Unknown char $it")
    }
}.toInt(2)

// clockwise
private fun TransformedImage.rotate(count: Int): TransformedImage {
    if (count == 0) return this
    val base = rotate(count - 1)
    // reverse those where we switch orientation (numbers are read from left to right, top to bottom,
    // so a change between those two is fine, but no other change)
    return TransformedImage(
        id,
        original,
        size,
        base.flipped,
        base.rotation + 1,
        base.leftEdge.reverse(size),
        base.topEdge,
        base.rightEdge.reverse(size),
        base.bottomEdge
    )
}

// flip along the vertical axis
private fun TransformedImage.flip(): TransformedImage {
    return TransformedImage(
        id,
        original,
        size,
        !flipped,
        rotation,
        topEdge.reverse(size),
        leftEdge,
        bottomEdge.reverse(size),
        rightEdge
    )
}

private fun Int.reverse(size: Int): Int = toString(2).padStart(size, '0').reversed().toInt(2)

private fun partOne(m: Grid<TransformedImage>): Long {
    return m[0, 0]!!.id.toLong() * m[0, m.height - 1]!!.id * m[m.width - 1, 0]!!.id * m[m.width - 1, m.height - 1]!!.id
}

private enum class State {
    DOT, MONSTER, HASH
}

private fun partTwo(m: Grid<TransformedImage>, monster: String): Int {
    // process the images
    val processedImages: Grid<Image> = m.map {
        it.applyTransform()
          .cutoffEdges()
    }

    // stitch them together
    val imgSize = processedImages[0, 0]!!.image.width
    val bigSize = m.width * imgSize
    val image: ImageData = Grid.createGrid(bigSize, bigSize) { x, y ->
        val img = processedImages[x / imgSize, y / imgSize]!!
        val pixel = img.image[x % imgSize, y % imgSize]!!
        pixel
    }

    val possibleImages = sequence {
        for (i in 0..3) {
            yield(image.rotate(i))
            yield(image.flip().rotate(i))
        }
    }

    val spec = prepareMonster(monster)

    val state = possibleImages.toList().also {
        println("Trying ${it.size} images")
    }.map {
        findSeaMonsters(bigSize, it, spec)
    }.firstOrNull {
        // are there any monster found?
        it.pointValues().any { it.second == State.MONSTER }
    } ?: error("No monsters found!")

    return state.pointValues().count { it.second == State.HASH }
}

private fun findSeaMonsters(
    size: Int,
    image: ImageData,
    spec: MonsterSpec
): Grid<State> {
    val states: Array<State?> = arrayOfNulls(size * size)
    fun getState(x: Int, y: Int): State? = states[x + y * size]
    fun setState(x: Int, y: Int, state: State) {
        states[x + y * size] = state
    }

    for (y in 0 until size) {
        for (x in 0 until size) {
            if (getState(x, y) != null) continue // already done

            when (image[x, y]) {
                '.' -> setState(x, y, State.DOT)
                '#' -> {
                    // check if this is the start of monster
                    val locations = spec.offsets.map { Point(x + it.x, y + it.y) }
                    if (locations.all { (lx, ly) ->
                            image[lx, ly] == '#' && getState(lx, ly) == null
                        }) {
                        // monster!
                        setState(x, y, State.MONSTER)
                        locations.forEach { (lx, ly) ->
                            setState(lx, ly, State.MONSTER)
                        }
                    } else {
                        setState(x, y, State.HASH)
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    return Grid(size, size, states.asList() as List<State>)
}

private data class MonsterSpec(
    val offsets: List<Point>
)

private fun prepareMonster(monster: String): MonsterSpec {

    // first line has to have a '#'
    val lines = monster.lines()

    val start = lines.first().indexOf('#')

    // compute relative distances to each other
    val offsets = lines.flatMapIndexed { y: Int, s: String ->
        s.mapIndexedNotNull { x: Int, c: Char ->
            when (c) {
                '#' -> Point(x - start, y)
                else -> null
            }
        }
    }

    return MonsterSpec(offsets)
}

private fun TransformedImage.applyTransform(): Image {
    var transformed = original.image
    if (flipped) {
        transformed = transformed.flip()
    }
    transformed = transformed.rotate(rotation)
    return Image(
        id,
        transformed
    )
}

private fun ImageData.flip(): ImageData = Grid.createGrid(width, height) { x, y ->
    get(width - 1 - x, y)!!
}

private fun ImageData.rotate(count: Int): ImageData {
    if (count == 0) return this

    val base = rotate(count - 1)
    return Grid.createGrid(base.width, base.height) { x, y ->
        base[y, height - 1 - x]!!
    }
}

private fun Image.cutoffEdges(): Image = Image(
    id,
    Grid.createGrid(image.width - 2, image.height - 2) { x, y -> image[x + 1, y + 1]!! }
)

private fun assembleImage(images: List<Image>): Grid<TransformedImage> {
    fun buildMap(
        mapSoFar: Grid<TransformedImage?>,
        imagesLeft: List<TransformedImage>
    ): Grid<TransformedImage>? {
        // find next location to do:
        val target = mapSoFar.pointValues().firstOrNull { it.second == null }

        @Suppress("UNCHECKED_CAST") // check by the condition
        if (target == null) return mapSoFar as Grid<TransformedImage>

        val x = target.first.x
        val y = target.first.y

        // check the neighbours
        val actualOptions = imagesLeft.toMutableList()
        mapSoFar[x, y - 1]?.let { img ->
            actualOptions.retainAll {
                it.topEdge == img.bottomEdge
            }
        }
        mapSoFar[x, y + 1]?.let { img ->
            actualOptions.retainAll {
                it.bottomEdge == img.topEdge
            }
        }
        mapSoFar[x - 1, y]?.let { img ->
            actualOptions.retainAll {
                it.leftEdge == img.rightEdge
            }
        }
        mapSoFar[x + 1, y]?.let { img ->
            actualOptions.retainAll {
                it.rightEdge == img.leftEdge
            }
        }

        return actualOptions
            .asSequence()
            .mapNotNull { img ->
                val newMap = mapSoFar.set(x, y, img)
                val remaining = imagesLeft.filterNot { it.id == img.id }
                buildMap(newMap, remaining)
            }.firstOrNull()
    }

    val sizeOfBigImage = sqrt(images.size.toFloat()).roundToInt()
    val init: Grid<TransformedImage?> = Grid.createGrid(sizeOfBigImage, sizeOfBigImage) { _, _ -> null }

    val allOptions = images.map(Image::toTransformed).flatMap { it.allPossible() }
    return buildMap(init, allOptions) ?: error("Failed to solve puzzle")
}

// some of the helper classes I have come to rely on

data class Point(val x: Int, val y: Int)

data class Grid<T>(
    val width: Int,
    val height: Int,
    private val data: List<T>
) {
    companion object {
        fun <T> createGrid(width: Int, height: Int, supplier: (Int, Int) -> T): Grid<T> =
            Grid(width, height, (0 until height).flatMap { y -> (0 until width).map { x -> supplier(x, y) } })
    }

    private fun inBounds(x: Int, y: Int) = x in 0..width - 1 && y in 0 until height
    operator fun get(x: Int, y: Int): T? = if (inBounds(x, y)) data[x + y * width] else null
    fun set(x: Int, y: Int, t: T): Grid<T> = Grid(width, height, data.toMutableList().also {
        it[x + y * width] = t
    })

    fun pointValues(): Sequence<Pair<Point, T>> = sequence {
        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                @Suppress("UNCHECKED_CAST")
                yield(Point(x, y) to get(x, y) as T)
            }
        }
    }

    fun topRow(): List<T> = (0 until width).map { x -> get(x, 0)!! }
    fun botRow(): List<T> = (0 until width).map { x -> get(x, height - 1)!! }
    fun leftCol(): List<T> = (0 until height).map { y -> get(0, y)!! }
    fun rightCol(): List<T> = (0 until height).map { y -> get(width - 1, y)!! }

    fun <R> map(f: (T) -> R): Grid<R> = Grid(width, height, data.map(f))
}