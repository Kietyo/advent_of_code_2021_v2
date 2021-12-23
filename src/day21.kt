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

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day21_test")
    val mainInput = readInput("day21")

    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}