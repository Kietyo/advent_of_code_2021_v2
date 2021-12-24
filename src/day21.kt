enum class Player {
    PLAYER_1,
    PLAYER_2;
}

fun main() {
    class Dice {
        var curr = 0L
        fun roll(): Long {
            curr++
            if (curr >= 101) {
                curr = 1
            }
            return curr
        }
    }

    class Board(
        // Nominal number (index starts at 1)
        currSpaceNominal: Long
    ) {
        val maxSpaces = 10

        // Ordinal numbers start at 0
        private var currSpaceOrdinal = currSpaceNominal - 1

        fun increment(num: Long) {
            currSpaceOrdinal = (currSpaceOrdinal + num) % maxSpaces
        }

        fun getCurrSpaceNominal(): Long {
            return currSpaceOrdinal + 1
        }
    }

    fun part1(inputs: List<String>) {
        val regex = """Player \d starting position: (\d)""".toRegex()
        val player1Pos = regex.matchEntire(inputs.first())!!.destructured.let { (pos) ->
            pos.toLong()
        }

        val player2Pos = regex.matchEntire(inputs.last())!!.destructured.let { (pos) ->
            pos.toLong()
        }

        val board1 = Board(player1Pos)
        var player1Score = 0L
        val board2 = Board(player2Pos)
        var player2Score = 0L
        val dice = Dice()

        var player1Turn = true
        var numRolls = 0
        while (true) {
            val threeDiceRolls = listOf(
                dice.roll(),
                dice.roll(),
                dice.roll(),
            )
            numRolls += 3
            val sum = threeDiceRolls.sum()
            if (player1Turn) {
                board1.increment(sum)
                val score = board1.getCurrSpaceNominal()
                player1Score += score
                println(
                    "player 1 rolls: $threeDiceRolls, sum: $sum, score to add: $score, " +
                            "player score: $player1Score, numRolls: $numRolls"
                )
                if (player1Score >= 1000) {
                    break
                }
            } else {
                board2.increment(sum)
                val score = board2.getCurrSpaceNominal()
                player2Score += score
                println(
                    "player 2 rolls: $threeDiceRolls, sum: $sum, score to add: $score, " +
                            "player score: $player2Score, numRolls: $numRolls"
                )
                if (player2Score >= 1000) {
                    break
                }
            }
            player1Turn = !player1Turn
        }

        println("score:")
        if (player1Turn) {
            println(player2Score * numRolls)
        } else {
            println(player1Score * numRolls)
        }
    }

    data class State(val p1Score: Int, val p1Board: Int, val p2Score: Int, val p2Board: Int)

    val dp = mutableMapOf<State, Pair<Long, Long>>()

    fun calculatePart2(state: State):
            Pair<Long, Long> {
        if (dp.containsKey(state)) {
            return dp[state]!!
        }

        var numP1Wins = 0L
        var numP2Wins = 0L

        repeat(3) { p1d1 ->
            repeat(3) { p1d2 ->
                repeat(3) { p1d3 ->
                    val p1DiceRolls = listOf(p1d1 + 1, p1d2 + 1, p1d3 + 1)
                    val p1Movement = p1DiceRolls.sum()
                    val p1BoardAfterMovement = (state.p1Board + p1Movement - 1) % 10 + 1
                    val p1NewScore = state.p1Score + p1BoardAfterMovement
                    if (p1NewScore >= 21) {
                        numP1Wins++
                    } else {
                        repeat(3) { p2d1 ->
                            repeat(3) { p2d2 ->
                                repeat(3) { p2d3 ->
                                    val p2DiceRolls = listOf(p2d1 + 1, p2d2 + 1, p2d3 + 1)
                                    val p2Movement = p2DiceRolls.sum()
                                    val p2BoardAfterMovement =
                                        (state.p2Board + p2Movement - 1) % 10 + 1
                                    val p2NewScore = state.p2Score + p2BoardAfterMovement
                                    if (p2NewScore >= 21) {
                                        numP2Wins++
                                    } else {
                                        val res = calculatePart2(
                                            State(
                                                p1NewScore,
                                                p1BoardAfterMovement,
                                                p2NewScore,
                                                p2BoardAfterMovement
                                            )
                                        )
                                        numP1Wins += res.first
                                        numP2Wins += res.second
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return Pair(numP1Wins, numP2Wins).apply {
            dp[state] = this
        }
    }

    fun part2(inputs: List<String>) {
        val regex = """Player \d starting position: (\d)""".toRegex()
        val player1Pos = regex.matchEntire(inputs.first())!!.destructured.let { (pos) ->
            pos.toInt()
        }

        val player2Pos = regex.matchEntire(inputs.last())!!.destructured.let { (pos) ->
            pos.toInt()
        }

        println(calculatePart2(State(0, player1Pos, 0, player2Pos)))
    }

    val testInput = readInput("day21_test")
    val mainInput = readInput("day21")

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    part2(mainInput)
}
//148747830493442
//89305072914203