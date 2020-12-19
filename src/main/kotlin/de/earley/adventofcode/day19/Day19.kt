package de.earley.adventofcode.day19

import de.earley.adventofcode.parser.*
import de.earley.adventofcode.split

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val (ruleText, messages) = input.split { it.isBlank() }
    val rules = ruleText.map(::parseRule).toMap()

    println("Part one: ${partOne(messages, rules)}")
    println("Part two: ${partTwo(messages, rules)}")
}

private sealed class Rule
private data class RuleSequence(val subRules : List<Int>) : Rule()
private data class LetterRule(val letter : Char) : Rule()
private data class ChoiceRule(val choices : List<Rule>) : Rule()

private object Grammar {
    private val numberRule : Parser<Char, Rule> = many(number, delimiter = ws).map(::RuleSequence)
    private val letterRule : Parser<Char, Rule> = char('"').void() + letter.map(::LetterRule) + char('"').void()
    private val simpleRule : Parser<Char, Rule> = letterRule or numberRule
    private val rule : Parser<Char, Rule> = many(simpleRule, delimiter = ws + char('|') + ws).map(::ChoiceRule)
    val numberedRule : Parser<Char, Pair<Int, Rule>> = number + char(':').void() + ws + rule
}

private fun parseRule(line : String) : Pair<Int, Rule> = when (val result = Grammar.numberedRule.deriveAll(line)) {
    is ParseResult.Ok -> result.set().single()
    is ParseResult.Error -> throw Exception(result.error.toString())
}

private fun partOne(tests : List<String>, rules: Map<Int, Rule>) : Int {
    val parser = rules[0]!!.toParser(0, rules)
    return tests.count {
        parser.deriveAll(it) is ParseResult.Ok
    }
}

// if index is null this is not a top level rule
private fun Rule.toParser(index: Int?, rules: Map<Int, Rule>, done: Map<Int, Parser<Char, Any>> = emptyMap()) : Parser<Char, Any> = recursive { thisParser ->
    val newDone = if (index != null) {
        done + (index to thisParser)
    } else done
    when (this) {
        is RuleSequence -> subRules.map {
            if (done.contains(it)) done[it]!! else rules[it]!!.toParser(it, rules, done)
        }.reduce { a, b -> a + b }
        is LetterRule -> char(letter)
        is ChoiceRule -> {
            or(choices.map { it.toParser(null, rules, newDone) })
        }
    }
}

private fun partTwo(messages: List<String>, rules: Map<Int, Rule>): Int {
    // patch the rules
    val newRules = listOf("8: 42 | 42 8", "11: 42 31 | 42 11 31")
    val actualRules = rules.toMutableMap().apply {
        putAll(
            newRules.map(::parseRule)
        )
    }
    return partOne(messages, actualRules)
}
