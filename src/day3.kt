fun main() {

    data class BitCountOutput(
        val zeroBitCount: IntArray,
        val oneBitCount: IntArray
    ) {
        fun getMostCommonBitForIndex(i: Int): Char {
            return if (zeroBitCount[i] > oneBitCount[i]) '0' else '1'
        }
    }

    fun getBitCounts(inputs: List<String>): BitCountOutput {
        val length = inputs.first().length

        val zeroBitCount = IntArray(length)
        val oneBitCount = IntArray(length)

        for (input in inputs) {
            println(input)
            val num = input.toInt(2)
            val convertBack = num.toString(2).padStart(length, '0')
            val bitsSplit = input.split("")
            val chars = input.toCharArray()
            for ((i, char) in chars.withIndex()) {
                when (char) {
                    '0' -> zeroBitCount[i]++
                    '1' -> oneBitCount[i]++
                    else -> TODO()
                }
            }
            //            println(
            //                "num: $num, converted: $convertBack, bitsSplit: $bitsSplit, bitSplitLength: " +
            //                        "${bitsSplit.size}, chars: ${chars.joinToString()}"
            //            )

        }

        return BitCountOutput(
            zeroBitCount, oneBitCount
        )

    }

    fun part1(inputs: List<String>) {

        val bitCounts = getBitCounts(inputs)


        val gammaBits = bitCounts.zeroBitCount.zip(bitCounts.oneBitCount).fold("") { acc, pair ->
            acc + if (pair.first > pair.second) "0" else "1"
        }
        val epsilonBits = gammaBits.toCharArray().fold("") { acc, char ->
            acc + when (char) {
                '0' -> '1'
                '1' -> '0'
                else -> TODO()
            }
        }

        println(gammaBits)
        println(epsilonBits)

        val gammaRate = gammaBits.toInt(2)
        val epsilonRate = epsilonBits.toInt(2)
        val powerConsumption = gammaRate * epsilonRate
        println(
            """
            gammaRate: $gammaRate
            epsilonRate: $epsilonRate
            powerConsumption: $powerConsumption
        """.trimIndent()
        )
    }

    fun calculateOxygenRate(
        inputs: List<String>
    ): Int {
        val length = inputs.first().length
        val currentCandidates = inputs.toMutableList()

        for (i in 0 until length) {
            val bitCountOutput = getBitCounts(currentCandidates)
            val mostCommonBit = bitCountOutput.getMostCommonBitForIndex(i)
            currentCandidates.removeIf {
                it[i] != mostCommonBit
            }

            println("i: $i, currentCandidates: $currentCandidates")

            val uniqueCandidatesLeft = currentCandidates.toSet()

            if (uniqueCandidatesLeft.size == 1) {
                return uniqueCandidatesLeft.first().toInt(2)
            }
        }

        TODO()
    }

    fun calculateScubberRate(
        inputs: List<String>
    ): Int {
        val length = inputs.first().length
        val currentCandidates = inputs.toMutableList()

        for (i in 0 until length) {
            val bitCountOutput = getBitCounts(currentCandidates)
            val mostCommonBit = bitCountOutput.getMostCommonBitForIndex(i)
            currentCandidates.removeIf {
                it[i] == mostCommonBit
            }

            println("i: $i, currentCandidates: $currentCandidates")

            val uniqueCandidatesLeft = currentCandidates.toSet()

            if (uniqueCandidatesLeft.size == 1) {
                return uniqueCandidatesLeft.first().toInt(2)
            }
        }

        TODO()
    }

    fun part2(inputs: List<String>) {
        val oxygenRate = calculateOxygenRate(inputs)
        val scubberRate = calculateScubberRate(inputs)
        val lifeSupportRating = oxygenRate * scubberRate

        println(
            """
            oxygenRate: $oxygenRate
            scubberRate: $scubberRate
            lifeSupportRating: $lifeSupportRating
        """.trimIndent()
        )
    }

    val mainInput = readInput("day3")
    val testInput = readInput("day3_test")

    //    part1(testInput)
    //            part1(mainInput)

    //    part2(testInput)
    part2(mainInput)
}