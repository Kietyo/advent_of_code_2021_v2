import kotlin.math.max

fun main() {
    data class TargetArea(
        val xRange: IntRange,
        val yRange: IntRange
    )

    data class SimulationResult(
        val initialVelocity: Pair<Long, Long>,
        val reachedTarget: Boolean,
        val maxYReached: Long
    )

    fun simulateTrajectory(
        initialPosition: Pair<Long, Long>,
        initialVelocity: Pair<Long, Long>,
        targetArea: TargetArea
    ): SimulationResult {
        val maxX = targetArea.xRange.last
        val minY = targetArea.yRange.first

        var currX = initialPosition.first
        var currY = initialPosition.second

        var xVelocity = initialVelocity.first
        var yVelocity = initialVelocity.second

        var maxYReached = currY

        var step = 0
        var enteredTargetArea = false
        while (currX < maxX && currY > minY) {
            if (currX !in targetArea.xRange && xVelocity == 0L) {
                break
            }

            currX += xVelocity
            currY += yVelocity
            maxYReached = max(maxYReached, currY)
            //            println("step $step, pos: ($currX, $currY), velocity: ($xVelocity, $yVelocity), maxX:" +
            //                    " $maxX, minY: $minY, currX < maxX: ${currX < maxX}, currY > minY: ${currY >
            //                            minY}, currX < " +
            //                    "maxX && currY > minY: ${currX <
            //                            maxX && currY > minY}")

            xVelocity = max(xVelocity - 1, 0)
            yVelocity -= 1

            if (currX in targetArea.xRange && currY in targetArea.yRange) {
                enteredTargetArea = true
                break
            }

            step++
        }

        if (enteredTargetArea) {
            println("Entered target area!")
        } else {
            println("Failed to enter target area!")
        }

        return SimulationResult(initialVelocity, enteredTargetArea, maxYReached)
    }

    fun part1(inputs: List<String>) {
        //        val area = TargetArea(20..30, -10..-5)
        val area = TargetArea(81..129, -150..-108)


        val initialPosition = 0L to 0L

        //        simulateTrajectory(initialPosition, 1 to 100, area)

        var maxYReached = 0L
        var bestResult: SimulationResult? = null

        for (x in 1L..1000L) {
            for (y in 1L..1000L) {
                val result = simulateTrajectory(initialPosition, x to y, area)
                if (result.reachedTarget && result.maxYReached > maxYReached) {
                    maxYReached = result.maxYReached
                    bestResult = result
                }
            }
        }

        println(
            """
            maxYReached: $maxYReached
            bestResult: $bestResult
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {
        //                val area = TargetArea(20..30, -10..-5)
        val area = TargetArea(81..129, -150..-108)


        val initialPosition = 0L to 0L
        val initialVelocity = 1 to 100

        //        simulateTrajectory(initialPosition, 1 to 100, area)

        var goodInitialVelocities = mutableListOf<Pair<Long, Long>>()

        for (x in -1000L..1000L) {
            for (y in -1000L..1000L) {
                val result = simulateTrajectory(initialPosition, x to y, area)
                if (result.reachedTarget) {
                    goodInitialVelocities.add(result.initialVelocity)
                }
            }
        }

        println(
            """
            goodInitialVelocities: $goodInitialVelocities,
            goodInitialVelocities.size: ${goodInitialVelocities.size}
        """.trimIndent()
        )
    }

    val testInput = readInput("day17_test")
    val mainInput = readInput("day17")

    //    part1(testInput)
    //    part1(mainInput)
    //
    part2(testInput)
    //    part2(mainInput)
}