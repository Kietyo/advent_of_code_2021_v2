import kotlin.math.absoluteValue
import kotlin.math.min

fun main() {
    fun part1(inputs: List<String>) {
        val nums = inputs.first().split(",").map { it.toInt() }
        val minNum = nums.minOrNull()!!
        val maxNum = nums.maxOrNull()!!

        var minCost = Int.MAX_VALUE
        for (i in minNum..maxNum) {
            var totalCost = 0
            for (num in nums) {
                totalCost += (num - i).absoluteValue
            }
            minCost = min(minCost, totalCost)
        }
        println(
            """
            nums: $nums
            minNum: $minNum
            maxNum: $maxNum
            minCost: $minCost
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {
        val nums = inputs.first().split(",").map { it.toInt() }
        val minNum = nums.minOrNull()!!
        val maxNum = nums.maxOrNull()!!

        fun costOfMovement(numSteps: Int): Int {
            return (numSteps + 1) * numSteps / 2
        }

        var minCost = Long.MAX_VALUE
        for (i in minNum..maxNum) {
            var totalCost = 0L
            for (num in nums) {
                val cost = costOfMovement((num - i).absoluteValue)
                totalCost += cost
            }
            minCost = min(minCost, totalCost)
        }
        println(
            """
            nums: $nums
            minNum: $minNum
            maxNum: $maxNum
            minCost: $minCost
        """.trimIndent()
        )
    }

    val testInput = readInput("day7_test")
    val mainInput = readInput("day7")

    //    part1(testInput)
    //    part1(mainInput)

    //    part2(testInput)
    part2(mainInput)
}