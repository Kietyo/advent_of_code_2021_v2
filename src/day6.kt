import java.math.BigInteger

fun main() {
    fun part1(inputs: List<String>) {
        val inputNumbers = inputs.first().split(",").map { it.toInt() }.toMutableList()
        println(inputNumbers)

        println("Initial state: $inputNumbers")
        for (day in 0 until 80) {
            for (i in inputNumbers.indices) {
                inputNumbers[i]--

            }
            for (i in inputNumbers.indices) {
                if (inputNumbers[i] == -1) {
                    inputNumbers[i] = 6
                    inputNumbers.add(8)
                }
            }
            println("After ${day + 1} day: $inputNumbers")
            println("num fishes: ${inputNumbers.size}")
        }
    }

    fun part2(inputs: List<String>) {
        // Map of repro day to num fish
        var fishMap = mutableMapOf<Int, BigInteger>()
        val inputNumbers = inputs.first().split(",").map { it.toInt() }
        println(inputNumbers)

        inputNumbers.forEach {
            fishMap[it] = fishMap.getOrDefault(it, BigInteger.ZERO) + BigInteger.ONE
        }
        println(fishMap)

        println("Initial state: $inputNumbers")
        for (day in 0 until 256) {
            val newFishMap = mutableMapOf<Int, BigInteger>()
            for (key in fishMap.keys) {
                newFishMap[key - 1] = fishMap[key]!!
            }
            if (newFishMap.containsKey(-1)) {
                val numFishesBirthed = newFishMap[-1]!!
                newFishMap[6] = newFishMap.getOrDefault(6, BigInteger.ZERO) + numFishesBirthed
                newFishMap[8] = newFishMap.getOrDefault(8, BigInteger.ZERO) + numFishesBirthed
                newFishMap.remove(-1)
            }
            val numFishes = newFishMap.values.fold(BigInteger.ZERO) { acc, it ->
                acc.add(it)
            }
            println("After ${day + 1}, num fishes: $numFishes")
            println(newFishMap)
            fishMap = newFishMap
        }
    }

    fun part2WithLong(inputs: List<String>) {
        // Map of repro day to num fish
        var fishMap = mutableMapOf<Int, Long>()
        val inputNumbers = inputs.first().split(",").map { it.toInt() }
        println(inputNumbers)

        inputNumbers.forEach {
            fishMap[it] = fishMap.getOrDefault(it, 0L) + 1L
        }
        println(fishMap)

        println("Initial state: $inputNumbers")
        for (day in 0 until 256) {
            val newFishMap = mutableMapOf<Int, Long>()
            for (key in fishMap.keys) {
                newFishMap[key - 1] = fishMap[key]!!
            }
            if (newFishMap.containsKey(-1)) {
                val numFishesBirthed = newFishMap[-1]!!
                newFishMap[6] = newFishMap.getOrDefault(6, 0L) + numFishesBirthed
                newFishMap[8] = newFishMap.getOrDefault(8, 0L) + numFishesBirthed
                newFishMap.remove(-1)
            }
            val numFishes = newFishMap.values.sum()
            println("After ${day + 1}, num fishes: $numFishes")
            println(newFishMap)
            fishMap = newFishMap
        }
    }

    val testInput = readInput("day6_test")
    val mainInput = readInput("day6")

    //    part1(testInput)
    //    part1(mainInput)

    //    part2(testInput)
    part2(mainInput)
}

