package de.earley.adventofcode.day18

import de.earley.adventofcode.*

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    println("Part one: ${input.map(::partOne).sumByLong { it.calc() }}")
    println("Part two: ${input.map(::partTwo).sumByLong { it.calc() }}")
}

private enum class Op(val s: String) {
    MULTIPLY("*"), ADD("+");

    override fun toString(): String = s
}

private sealed class Expr
private data class NumExpr(val value: Int) : Expr() {
    override fun toString(): String = value.toString()
}
private data class BinOp(val left: Expr, val op: Op, val right: Expr) : Expr() {
    override fun toString(): String = "($left $op $right)"
}

private object Grammar {
    private val numExpr: Parser<Expr> = number.map(::NumExpr)
    private fun parenExpr(e: Parser<Expr>): Parser<Expr> = char('(').void() + ws + e + ws + char(')').void()
    private val add: Parser<Op> = char('+').map { Op.ADD }
    private val mult: Parser<Op> = char('*').map { Op.MULTIPLY }
    private val op: Parser<Op> = add or mult

    private fun factor(e: Parser<Expr>): Parser<Expr> = numExpr or parenExpr(e)

    // no precedence
    private val expr: Parser<Expr> = rec { expr ->
        (factor(expr) + ws + many(op + ws + factor(expr) + ws)).map { left, rights ->
            rights.fold(left) { acc, cur ->
                BinOp(acc, cur.first, cur.second)
            }
        }
    }

    val partOne : Parser<Expr> = parenExpr(expr)

    // with precedence
    private val term: Parser<Expr> = lazy {
        (factor(exprPrecedence) + ws + many(add + ws + factor(exprPrecedence) + ws)).map { left, rights ->
            rights.fold(left) { acc, cur ->
                BinOp(acc, cur.first, cur.second)
            }
        }
    }

    private val exprPrecedence: Parser<Expr> = (term + ws + many(mult + ws + term + ws)).map { left, rights ->
        rights.fold(left) { acc, cur ->
            BinOp(acc, cur.first, cur.second)
        }
    }

    val partTwo : Parser<Expr> = parenExpr(exprPrecedence)
}

// wrap in parens because my parser does not backtrack well
private fun partOne(input: String): Expr = Grammar.partOne.parse("($input)")
private fun partTwo(input: String): Expr = Grammar.partTwo.parse("($input)")


private fun Expr.calc(): Long = when (this) {
    is NumExpr -> value.toLong()
    is BinOp -> {
        val l = left.calc()
        val r = right.calc()
        when (op) {
            Op.MULTIPLY -> l * r
            Op.ADD -> l + r
        }
    }
}