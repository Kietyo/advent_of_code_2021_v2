data class SegmentDisplay(
    val chars: Set<Char>,
) {
    constructor(str: String) : this(
        str.toSet()
    )

    val numSegmentsActive = chars.size

    companion object {
        val ZERO = SegmentDisplay(
            "abcefg"
        )
        val ONE = SegmentDisplay(
            "cf"
        )
        val TWO = SegmentDisplay(
            "acdeg"
        )
        val THREE = SegmentDisplay(
            "acdfg"
        )
        val FOUR = SegmentDisplay(
            "bcdf"
        )
        val FIVE = SegmentDisplay(
            "abdfg"
        )
        val SIX = SegmentDisplay(
            "abdefg"
        )
        val SEVEN = SegmentDisplay(
            "acf"
        )
        val EIGHT = SegmentDisplay(
            "abcdefg"
        )
        val NINE = SegmentDisplay(
            "abcdfg"
        )

        val segmentNumToDigitList = listOf<SegmentDisplay>(
            ONE,
            TWO,
            THREE,
            FOUR,
            FIVE,
            SIX,
            SEVEN,
            EIGHT,
            NINE
        ).groupBy { it.numSegmentsActive }

        fun segmentNumToPossibleDigit(num: Int): SegmentDisplay? {
            val digitList = segmentNumToDigitList.getOrDefault(num, emptyList())
            if (digitList.size == 1) {
                return digitList.first()
            }
            return null
        }
    }
}

fun main() {
    val configToNum: Map<Set<Char>, Int> = mapOf(
        "abcefg".toSet() to 0,
        "cf".toSet() to 1,
        "acdeg".toSet() to 2,
        "acdfg".toSet() to 3,
        "bcdf".toSet() to 4,
        "abdfg".toSet() to 5,
        "abdefg".toSet() to 6,
        "acf".toSet() to 7,
        "abcdefg".toSet() to 8,
        "abcdfg".toSet() to 9,
    )

    data class PuzzleInput(
        val signalPatterns: List<String>,
        val outputValues: List<String>
    ) {
        val signalPatternsLetterCounts: Map<Char, Int> =
            signalPatterns.flatMap { it.toSet() }.groupingBy { it }.eachCount()

        fun segment1(): String {
            return signalPatterns.first { it.length == 2 }
        }

        fun segment4(): String {
            return signalPatterns.first { it.length == 4 }
        }

        fun segment7(): String {
            return signalPatterns.first { it.length == 3 }
        }

        fun segment8(): String {
            return signalPatterns.first { it.length == 7 }
        }

        fun segmentA(): Char {
            return (segment7().toSet() - segment1().toSet()).first()
        }

        fun segmentB(): Char {
            return signalPatternsLetterCounts.firstNotNullOf {
                if (it.value == 6) it.key else null
            }
        }

        fun segmentC(): Char {
            return (segment1().toSet() - segmentF()).first()
        }

        fun segmentD(): Char {
            val abcefg = setOf(
                segmentA(),
                segmentB(),
                segmentC(),
                segmentE(),
                segmentF(),
                segmentG()
            )
            val candidate = signalPatterns.first {
                it.length == 7 && abcefg.all { char ->
                    it.contains(char)
                }
            }
            return (candidate.toSet() - abcefg).first()
        }

        fun segmentE(): Char {
            return signalPatternsLetterCounts.firstNotNullOf {
                if (it.value == 4) it.key else null
            }
        }

        fun segmentF(): Char {
            return signalPatternsLetterCounts.firstNotNullOf {
                if (it.value == 9) it.key else null
            }
        }

        fun segmentG(): Char {
            val abcef = setOf(
                segmentA(),
                segmentB(),
                segmentC(),
                segmentE(),
                segmentF(),
            )
            val possibleZero = signalPatterns.first {
                it.length == 6 && abcef.all { char ->
                    it.contains(char)
                }
            }
            return (possibleZero.toSet() - abcef).first()
        }

        val signalMapping = mapOf(
            segmentA() to 'a',
            segmentB() to 'b',
            segmentC() to 'c',
            segmentD() to 'd',
            segmentE() to 'e',
            segmentF() to 'f',
            segmentG() to 'g',
        )

        fun outputValuesToActual(): Long {
            val actual = outputValues.map { it.toList().map { signalMapping[it] }.toSet() }.map {
                configToNum[it]!!
            }.joinToString("")
            return actual.toLong()
        }

    }

    fun part1(inputs: List<String>) {
        val puzzleInputs = inputs.map {
            val t1 = it.split(" | ")
            val signalPatterns = t1.first().split(" ")
            val outputValues = t1.last().split(" ")
            PuzzleInput(signalPatterns, outputValues)
        }

        var totalEasyDigits = 0
        for (puzzleInput in puzzleInputs) {
            val numEasyDigits = puzzleInput.outputValues.map { it.length }.mapNotNull {
                SegmentDisplay
                    .segmentNumToPossibleDigit(it)
            }.size
            println(
                """
                puzzleInput: $puzzleInput
                numEasyDigits: $numEasyDigits
            """.trimIndent()
            )
            totalEasyDigits += numEasyDigits
        }
        println("totalEasyDigits: $totalEasyDigits")
    }

    fun part2(inputs: List<String>) {
        val puzzleInputs = inputs.map {
            val t1 = it.split(" | ")
            val signalPatterns = t1.first().split(" ")
            val outputValues = t1.last().split(" ")
            PuzzleInput(signalPatterns, outputValues)
        }

        var total = 0L
        for (puzzleInput in puzzleInputs) {
            val actual = puzzleInput.outputValuesToActual()
            println(
                """
                puzzleInput: $puzzleInput
                actual: $actual
            """.trimIndent()
            )
            total += actual
        }
        println("total: $total")
    }


    val testInput = readInput("day8_test")
    val test2Input = readInput("day8_test2")
    val mainInput = readInput("day8")

    println(
        configToNum[setOf('f', 'c', 'c')]
    )

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(test2Input)
    //        part2(testInput)
    part2(mainInput)
}