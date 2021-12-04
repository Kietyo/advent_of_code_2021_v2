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

        override fun toString(): String {
            return board.zip(checkedBoard).joinToString("\n") {
                it.first.zip(it.second.toTypedArray()).joinToString {
                    "${it.first} (${it.second})"
                }
            }
        }
    }

    fun part1(inputs: List<String>) {

        for (input in inputs) {
            println(input)
        }

        val inputItr = inputs.iterator()
        val draws = inputItr.next()
        println(
            """
            draws: $draws
        """.trimIndent()
        )

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
            println(gameBoard)
            println("Board end:")
        }


    }

    val testInput = readInput("day4_test")

    part1(testInput)

}