fun main() {
    data class GameBoard(
        val board: List<IntArray>
    ) {
        val numRows = board.size
        val numColumns = board.first().size
        val checkedBoard = listOf(
            BooleanArray(5),
            BooleanArray(5),
            BooleanArray(5),
            BooleanArray(5),
            BooleanArray(5),
        )

        fun markNumber(toMark: Int) {
            for ((i, row) in board.withIndex()) {
                for ((j, num) in row.withIndex()) {
                    if (toMark == num) {
                        checkedBoard[i][j] = true
                    }
                }
            }
        }

        fun won(): Boolean {
            // Horizontal won
            for (row in checkedBoard) {
                if (row.all { it }) return true
            }

            // Check vertical won
            for (j in 0 until numColumns) {
                var wonColumn = true
                for (i in 0 until numRows) {
                    wonColumn = wonColumn && checkedBoard[i][j]
                }
                if (wonColumn) return true
            }

            return false
        }

        fun calculateScore(winningNumber: Int) {
            val sumOfUnmarkedNumbers = board.zip(checkedBoard).sumOf {
                it.first.zip(it.second.toTypedArray()).sumOf { elem ->
                    if (!elem.second) elem.first else 0
                }
            }
            val score = sumOfUnmarkedNumbers * winningNumber
            println(
                """
                sumOfUnmarkedNumbers: $sumOfUnmarkedNumbers
                score: $score
            """.trimIndent()
            )
        }

        override fun toString(): String {
            return board.zip(checkedBoard).joinToString("\n") {
                it.first.zip(it.second.toTypedArray()).joinToString {
                    "${it.first} (${it.second})"
                }
            }
        }
    }

    data class PuzzleState(
        val draws: List<Int>,
        val gameBoards: List<GameBoard>
    ) {
    }

    fun calculatePuzzleState(inputs: List<String>): PuzzleState {
        val inputItr = inputs.iterator()
        val draws = inputItr.next().split(",").map { it.toInt() }
        println(
            """
            draws: $draws
        """.trimIndent()
        )

        val gameBoards = buildList<GameBoard> {
            while (inputItr.hasNext()) {
                // Consume blank line
                inputItr.next()

                val board = mutableListOf<IntArray>()
                println("Board start:")
                for (i in 0 until 5) {
                    val line = inputItr.next()
                    val splitLine = line.split(" ").filter {
                        it.isNotEmpty()
                    }.map { it.toInt() }.toIntArray()
                    println(line)
                    println(splitLine.joinToString())
                    board.add(splitLine)
                }
                val gameBoard = GameBoard(board)
                this.add(gameBoard)
                println(gameBoard)
                println("Board end:")
            }
        }

        return PuzzleState(draws, gameBoards)
    }

    fun part1(inputs: List<String>) {

        val puzzleState = calculatePuzzleState(inputs)

        val drawsItr = puzzleState.draws.iterator()
        var drawNum: Int? = null
        while (!puzzleState.gameBoards.any { it.won() }) {
            drawNum = drawsItr.next()
            puzzleState.gameBoards.forEach { it.markNumber(drawNum!!) }
            println("Marking number $drawNum")
            puzzleState.gameBoards.forEachIndexed { index, gameBoard ->
                println("Gameboard $index")
                println(gameBoard)
                println()
            }
        }

        val winningBoard = puzzleState.gameBoards.first { it.won() }
        println("Winning number: ${drawNum!!}")
        println("Winning board:\n$winningBoard")
        winningBoard.calculateScore(drawNum!!)
    }

    fun part2(inputs: List<String>) {
        val puzzleState = calculatePuzzleState(inputs)
        val numGameBoards = puzzleState.gameBoards.size

        val candidates = puzzleState.gameBoards.toMutableList()

        val winningGameBoardsOrdered = mutableListOf<GameBoard>()
        val winningDrawNum = mutableListOf<Int>()

        val drawsItr = puzzleState.draws.iterator()
        var drawNum: Int? = null
        while (drawsItr.hasNext() && candidates.isNotEmpty()) {
            drawNum = drawsItr.next()
            candidates.forEach { it.markNumber(drawNum!!) }
            candidates.removeIf {
                if (it.won()) {
                    println("This game board won with drawNum=$drawNum!")
                    println(it)
                    winningGameBoardsOrdered.add(it)
                    winningDrawNum.add(drawNum!!)
                    true
                } else {
                    false
                }
            }
        }

        val lastWinningBoard = winningGameBoardsOrdered.last()
        val lastDraw = winningDrawNum.last()
        lastWinningBoard.calculateScore(lastDraw)
    }

    val testInput = readInput("day4_test")
    val input = readInput("day4")

    //    part1(testInput)
    //    part1(input)

    //    part2(testInput)
    part2(input)
}