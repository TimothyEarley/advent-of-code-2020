package de.earley.adventofcode.day21

import de.earley.adventofcode.*

fun main() {
    val foods = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(foodGrammar::parse)

    val allergensToFood = allergensToFood(foods)

    println("Part one: ${partOne(foods, allergensToFood)}")
    println("Part two:\n${partTwo(foods, allergensToFood)}")
}

private typealias Allergen = String
private typealias FoodName = String
private data class Food(
    val ingredients : List<FoodName>,
    val allergens : List<Allergen>
)

private val foodGrammar : Parser<Food> =
    (many(anyWord(), delimiter = space()) +
            string("(contains ").void() +
            many(anyWord(), delimiter = string(", ")) +
            char(')').void()).map(::Food)

private fun partOne(foods : List<Food>, allergensToFood: MutableMap<Allergen, Set<FoodName>>) : Int {

    val ingredientsWithAllergens = allergensToFood.values.flatten().toSet()

    val ingredientsWithoutAllergens = foods
        .flatMap { it.ingredients }
        .count { ! ingredientsWithAllergens.contains(it) }

    return ingredientsWithoutAllergens
}

private fun allergensToFood(foods: List<Food>): MutableMap<Allergen, Set<FoodName>> {
    val allergensToFood: MutableMap<Allergen, Set<FoodName>> = mutableMapOf()

    foods.forEach { food ->
        food.allergens.forEach { allergen ->
            allergensToFood.compute(allergen) { _, old ->
                old?.intersect(food.ingredients)
                    ?: food.ingredients.toSet()
            }
        }
    }
    return allergensToFood
}

private fun partTwo(foods: List<Food>, allergensToFood: MutableMap<Allergen, Set<FoodName>>): String {

    // assume there is a unique one to one mapping and we do not need to guess

    val mapping : MutableMap<FoodName, Allergen> = mutableMapOf()
    val todo : MutableList<Pair<Allergen, Set<FoodName>>> = allergensToFood.map { it.key to it.value }
        .toMutableList()

    while (todo.isNotEmpty()) {
        todo.sortBy { it.second.size }

        val next = todo.removeFirst()

        require(next.second.size == 1) { "We need a better algorithm!" }

        val ingredient = next.second.single()
        mapping[next.first] = ingredient
        todo.replaceAll { (allergen, ingredients) ->
            allergen to ingredients.filterNot { it == ingredient }.toSet()
        }
    }

    return mapping.entries.sortedBy {
        it.key
    }.joinToString(",") {
        it.value
    }
}
