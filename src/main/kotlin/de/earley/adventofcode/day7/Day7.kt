package de.earley.adventofcode.day7

import de.earley.adventofcode.*

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(::parseRule)


    println("Part one: ${solvePartOne(input)}")
    println("Part two: ${solvePartTwo(input)}")
}

private typealias BagColour = String

private data class Rule(
    val bagColour: BagColour,
    val contains: Map<BagColour, Int>
)

private object RuleGrammar {
    val bagColour: Parser<BagColour> =
        (anyWord() + space() + anyWord()).map { it.first + " " + it.second }
    val noMoreBags: Parser<Map<BagColour, Int>> = string("no other bags.").map { emptyMap() }
    val moreBags: Parser<Map<BagColour, Int>> = many(number() + space() + bagColour + space() + oneOf("bags", "bag"), delimiter = string(", ")).map {
         it.associate { (a, _) -> a.second to a.first }
    } + char('.').void()
    val ruleGrammar: Parser<Rule> = (bagColour + string(" bags contain ") + (noMoreBags or moreBags))
        .map { (a, bags) ->
            Rule(a.first, bags)
        }
}

private fun parseRule(line: String): Rule = RuleGrammar.ruleGrammar.parse(line)

private fun solvePartOne(rules : List<Rule>) : Int {

    // reverse the rule list to go from bag to list of where it can be placed
    val reverse : Map<BagColour, List<BagColour>> = rules.fold(emptyMap()) { accMap, rule ->
        rule.contains.keys.fold(accMap) { acc, bagColour ->
            acc.toMutableMap().apply {
                compute(bagColour) { _, old ->
                (old ?: emptyList()) + rule.bagColour
                }
            }
        }
    }

    val targets = reverse.getOrElse("shiny gold", ::emptyList).toMutableSet()
    val done = targets.toMutableSet()
    val toConsider = targets.toMutableSet()

    while (toConsider.isNotEmpty()) {
        val next = toConsider.removeFirst()
        done.add(next)
        reverse.getOrElse(next, ::emptyList)
            .filter { ! done.contains(it) }
            .forEach {
                targets.add(it)
                toConsider.add(it)
            }
    }

    return targets.size
}

private fun <E> MutableSet<E>.removeFirst() : E {
    val i = iterator()
    val e = i.next()
    i.remove()
    return e
}

private fun solvePartTwo(rules : List<Rule>) : Int {

    // cache
    val results : MutableMap<BagColour, Int> = mutableMapOf()

    val ruleMap : Map<BagColour, Map<BagColour, Int>> = rules.associate { it.bagColour to it.contains }

    fun solve(c : BagColour) : Int {
        if (results.contains(c)) return results[c]!!

        // find the rule
        val contains = ruleMap[c]!!

        // use the rule
        val result =
            contains.entries.sumBy {  it.value * solve(it.key) } + 1 // +1 is the bag itself

        results[c] = result
        return result
    }

    return solve("shiny gold") - 1 // - 1 because the gold bag does not count
}