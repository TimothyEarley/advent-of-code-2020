package de.earley.adventofcode.day20

import de.earley.adventofcode.split
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
        Image(id.toInt(), data)
    }

    val assembled = assembleImage(images)

    val test = images.first()

    require(
        test.toTransformed().flip().rotate(2).applyTransform().image ==
                test.image.flip().rotate(2)
    )


    println("Part one: ${partOne(assembled)}")
    println("Part two: ${partTwo(assembled, """
        |                  # 
        |#    ##    ##    ###
        |#  #  #  #  #  #   
    """.trimMargin())}")
}

private typealias ImageData = List<List<Char>>

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
    image.size, // square
    false,
    0,
    image.first().toInt(),
    image.map { it.last() }.toInt(),
    image.last().toInt(),
    image.map { it.first() }.toInt()
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
    // reverse thos where we switch orientaion (numbers are read from left to right, top to bottom,
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

private fun partOne(m: List<List<TransformedImage>>): Long {
    return m.first().first().id.toLong() * m.first().last().id * m.last().first().id * m.last().last().id
}

private enum class State {
    DOT, MONSTER, HASH
}

private fun partTwo(m: List<List<TransformedImage>>, monster: String): Int {
    // process the images
    val processedImages: List<List<Image>> = m.map {
        it.map {
            it.applyTransform()
                .addSpaceBorder()
        // .cutoffEdges()
        }
    }

    processedImages.forEachIndexed { y, list ->
        list.forEachIndexed { x, img ->
            println("x=$x, y=$y, id=${img.id}")
            println(img.image.joinToString("\n") {
                it.joinToString("")
            })
        }
    }

    // stitch them together (-2 for border)
    val imgSize = processedImages.first().first().image.size
    val bigSize = m.size * imgSize
    val image: ImageData = List(bigSize) { y ->
        List(bigSize) { x ->
            val img = processedImages[x / imgSize][y / imgSize]
            val pixel = img.image[y % imgSize][x % imgSize]
            pixel
        }
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
        println("Trying ")
        println(it.joinToString("\n") {
            it.joinToString("")
        })
        findSeaMonsters(bigSize, it, spec)
    }.first {
        // is there any monster found?
        it.flatten().any { it == State.MONSTER }
    }

    return state.flatten().count { it == State.HASH }
}

@OptIn(ExperimentalStdlibApi::class)
private fun Image.addSpaceBorder() : Image {
    val size = image.size
    return Image(
        id,
        buildList {
            add(List(size + 2) { ' ' })
            addAll(
                image.map {
                    buildList {
                        add(' ')
                        addAll(it)
                        add(' ')
                    }
                }
            )
            add(List(size + 2) { ' ' })
        }
    )
}

@Suppress("UNCHECKED_CAST") // we check it
private fun findSeaMonsters(
    size: Int,
    image: ImageData,
    spec: MonsterSpec
): Array<Array<State>> {
    val states: Array<Array<State?>> = Array(size) {
        arrayOfNulls(size)
    }

    for (x in 0 until size) {
        for (y in 0 until size) {
            if (states[y][x] != null) continue // already done

            when (image[y][x]) {
                '.' -> states[y][x] = State.DOT
                '#' -> {
                    // check if this is the start of monster
                    val locations = spec.offsets.map { (dx, dy) ->
                        (x + dx) to (y + dy)
                    }
                    if (locations.all { (lx, ly) ->
                            image.getOrNull(ly)?.getOrNull(lx) == '#'
                                    && states[ly][lx] == null
                        }) {
                        // monster!
                        states[y][x] = State.MONSTER
                        locations.forEach { (lx, ly) ->
                            states[ly][lx] = State.MONSTER
                        }
                    } else {
                        states[y][x] = State.HASH
                    }
                }
            }
        }
    }

    return states as Array<Array<State>>
}

private data class MonsterSpec(
    val origin: Pair<Int, Int>,
    val offsets : List<Pair<Int, Int>>
)

private fun prepareMonster(monster: String) : MonsterSpec {

    // first line has to have a '#'
    val lines = monster.lines()

    val start = lines.first().indexOf('#')

    // compute relative distances to each other
    val offsets = lines.flatMapIndexed { y: Int, s: String ->
        s.mapIndexedNotNull{ x:Int, c : Char ->
            if (y == 0 && x == start) null
            else when (c) {
                '#' -> (x - start) to y
                else -> null
            }
        }
    }

    return MonsterSpec(start to 0, offsets)
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

private fun ImageData.flip(): ImageData = map { it.reversed() }
private fun ImageData.rotate(count: Int): ImageData {
    if (count == 0) return this

    val base = rotate(count - 1)
    val size = base.size
    val rotated = base.map { it.toMutableList() }.toMutableList()
    for (x in 0 until size) {
        for (y in 0 until size) {
            rotated[(size - 1) - x][y] = base[x][y]
        }
    }
    return rotated
}

private fun Image.cutoffEdges(): Image = Image(
    id,
    image.drop(1).dropLast(1).map {
        it.drop(1).dropLast(1)
    }
)

private fun assembleImage(images: List<Image>): List<List<TransformedImage>> {
    fun buildMap(
        mapSoFar: List<List<TransformedImage?>>,
        imagesLeft: List<TransformedImage>
    ): List<List<TransformedImage>>? {
        // find next location to do:
        val target = mapSoFar.asSequence().map { ys ->
            ys.indexOfFirst { it == null }
        }.withIndex().firstOrNull {
            it.value != -1
        }
        @Suppress("UNCHECKED_CAST") // check by the condition
        if (target == null) return mapSoFar as List<List<TransformedImage>>

        val x = target.value
        val y = target.index

        // check the neighbours
        val actualOptions = imagesLeft.toMutableList()
        mapSoFar.getOrNull(y - 1)?.getOrNull(x)?.let { img ->
            actualOptions.retainAll {
                it.topEdge == img.bottomEdge
            }
        }
        mapSoFar.getOrNull(y + 1)?.getOrNull(x)?.let { img ->
            actualOptions.retainAll {
                it.bottomEdge == img.topEdge
            }
        }
        mapSoFar.getOrNull(y)?.getOrNull(x - 1)?.let { img ->
            actualOptions.retainAll {
                it.leftEdge == img.rightEdge
            }
        }
        mapSoFar.getOrNull(y)?.getOrNull(x + 1)?.let { img ->
            actualOptions.retainAll {
                it.rightEdge == img.leftEdge
            }
        }

        return actualOptions
            .asSequence()
            .mapNotNull { img ->
                val newMap = mapSoFar.toMutableList().apply {
                    this[y] = mapSoFar[y].toMutableList().apply {
                        this[x] = img
                    }
                }
                val remaining = imagesLeft.filterNot { it.id == img.id }
                buildMap(newMap, remaining)
            }.firstOrNull()
    }

    val sizeOfBigImage = sqrt(images.size.toFloat()).roundToInt()
    val init: List<List<TransformedImage?>> = List(sizeOfBigImage) {
        List(sizeOfBigImage) { null }
    }

    val allOptions = images.map(Image::toTransformed).flatMap { it.allPossible() }
    return buildMap(init, allOptions) ?: error("Failed to solve puzzle")
}