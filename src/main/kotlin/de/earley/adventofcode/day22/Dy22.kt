package de.earley.adventofcode.day22

import de.earley.adventofcode.split
import de.earley.adventofcode.sumByLong

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val (player1, player2) = input.split { it.isBlank() }

    val player1Deck = player1.drop(1).map(String::toInt)
    val player2Deck = player2.drop(1).map(String::toInt)
    val state = State(player1Deck, player2Deck)


    println("Part one: ${partOne(state)}")
    println("Part two: ${partTwo(state)}")
}

private typealias Deck = List<Card>
private typealias Card = Int

private enum class Player {
    Player1, Player2
}

private data class State(
    val player1 : Deck,
    val player2 : Deck,
) {
    fun isDone() : Boolean = player1.isEmpty() || player2.isEmpty()
    // assumes isDone
    fun winner() : Player = if (player1.isEmpty()) Player.Player2 else Player.Player1
    fun winnerDeck() : Deck = when (winner()) {
        Player.Player1 -> player1
        Player.Player2 -> player2
    }
}

private fun Deck.score() : Long = withIndex().sumByLong { (index, i) -> (size - index).toLong() * i }

private fun partOne(state : State) : Long {
    var current = state
    while (!current.isDone()) {
        current = current.step()
    }
    return current.winnerDeck().score()
}

private fun State.step(): State {
    val top1 = player1.first()
    val top2 = player2.first()
    return if (top1 > top2) {
        State(
            player1.drop(1) + top1 + top2,
            player2.drop(1)
        )
    } else {
        State(
            player1.drop(1),
            player2.drop(1) + top2 + top1
        )
    }
}

private fun partTwo(state: State): Long {
    var current = state
    while (!current.isDone()) {
        current = current.recStep()
    }
    return current.winnerDeck().score()
}

private fun State.recWinner(): Player {
    var current : State = this
    val rounds : MutableSet<State> = mutableSetOf()
    while (!current.isDone()) {
        // if this is a repeat of a previous round, player 1 wins
        if (rounds.contains(current)) {
            return Player.Player1
        }
        rounds.add(current)
        current = current.recStep()
    }
    return current.winner()
}

private fun State.recStep(): State {
    val top1 = player1.first()
    val top2 = player2.first()

    val winner : Player = if (player1.size - 1 >= top1 && player2.size - 1 >= top2) {
        // recursive game
        val subState = State(
            player1.drop(1).take(top1),
            player2.drop(1).take(top2),
        )
        subState.recWinner()
    } else {
        // normal game
        if (top1 > top2) Player.Player1 else Player.Player2
    }

    return when (winner) {
        Player.Player1 -> State(
            player1.drop(1) + top1 + top2 ,
            player2.drop(1)
        )
        Player.Player2 ->State(
            player1.drop(1),
            player2.drop(1) + top2 + top1
        )
    }
}
