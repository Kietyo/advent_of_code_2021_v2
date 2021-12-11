fun main() {
    fun getOpenSide(closing: Char): Char {
        return when (closing) {
            ')' -> '('
            ']' -> '['
            '}' -> '{'
            '>' -> '<'
            else -> TODO()
        }
    }

    fun getClosingSide(openSide: Char): Char {
        return when (openSide) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            '<' -> '>'
            else -> TODO()
        }
    }

    fun getClosingCorruptionScore(closing: Char): Long {
        return when (closing) {
            ')' -> 3L
            ']' -> 57L
            '}' -> 1197L
            '>' -> 25137L
            else -> TODO()
        }
    }

    fun getClosingAutocompleteScore(closing: Char): Long {
        return when (closing) {
            '(' -> 1L
            '[' -> 2L
            '{' -> 3L
            '<' -> 4L
            else -> TODO("$closing is not implemented")
        }
    }

    fun part1(inputs: List<String>) {
        val corruptedClosing = mutableListOf<Char>()
        for (input in inputs) {
            val charStack = mutableListOf<Char>()
            println(input)
            for (ch in input) {
                when (ch) {
                    '(', '[', '{', '<' -> charStack.add(ch)
                    ')', ']', '}', '>' -> {
                        val actualOpenSide = charStack.removeLast()
                        val expectedCloseSide = getClosingSide(actualOpenSide)
                        if (ch != expectedCloseSide) {
                            println(
                                "Expected $expectedCloseSide, but found $ch " +
                                        "instead."
                            )
                            corruptedClosing.add(ch)
                        }
                    }
                    else -> {
                        TODO("$ch is unsupported")
                    }
                }
                println(ch)
            }
        }

        val score = corruptedClosing.map { getClosingCorruptionScore(it) }.sum()

        println(
            """
            corruptedClosing: $corruptedClosing
            score: $score
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {
        val corruptedClosing = mutableListOf<Char>()
        val autoCompleteScores = mutableListOf<Long>()
        for (input in inputs) {
            var isCorrupted = false
            val charStack = mutableListOf<Char>()
            println(input)
            for (ch in input) {
                when (ch) {
                    '(', '[', '{', '<' -> charStack.add(ch)
                    ')', ']', '}', '>' -> {
                        val actualOpenSide = charStack.removeLast()
                        val expectedCloseSide = getClosingSide(actualOpenSide)
                        if (ch != expectedCloseSide) {
                            isCorrupted = true
                            println(
                                "Expected $expectedCloseSide, but found $ch " +
                                        "instead."
                            )
                            corruptedClosing.add(ch)
                        }
                    }
                    else -> {
                        TODO("$ch is unsupported")
                    }
                }
                //                println(ch)
            }

            if (isCorrupted || charStack.isEmpty()) {
                continue
            }

            var totalScore = 0L
            while (charStack.isNotEmpty()) {
                val currentClosing = charStack.removeLast()
                val autoCompleteScore = getClosingAutocompleteScore(currentClosing)
                totalScore = totalScore * 5 + autoCompleteScore
            }
            println(
                """
                totalScore: $totalScore
            """.trimIndent()
            )

            autoCompleteScores.add(totalScore)
        }

        val score = corruptedClosing.map { getClosingCorruptionScore(it) }.sum()

        autoCompleteScores.sort()

        val middleAutoCompleteScore = autoCompleteScores[autoCompleteScores.size / 2]

        println(
            """
            corruptedClosing: $corruptedClosing
            score: $score
            autoCompleteScores: $autoCompleteScores
            middleAutoCompleteScore: $middleAutoCompleteScore
        """.trimIndent()
        )
    }

    val testInput = readInput("day10_test")
    val mainInput = readInput("day10")

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    part2(mainInput)
}