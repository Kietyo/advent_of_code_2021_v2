fun part1(inputs: List<String>) {
    var depthDistance = 0
    var horizontalDistance = 0


    for (input in inputs) {
        val (action, numStr) = input.split(" ")
        val num = numStr.toInt()
        println(input)
        println("$action, $num")
        when (action) {
            "forward" -> {
                horizontalDistance += num
            }
            "down" -> {
                depthDistance += num
            }
            "up" -> {
                depthDistance -= num
            }
            else -> TODO()
        }
    }

    println("depthDistance: $depthDistance, horizontalDistance: $horizontalDistance")

    println(depthDistance * horizontalDistance)
}

fun part2(inputs: List<String>) {
    var depthDistance = 0
    var horizontalDistance = 0
    var aim = 0

    for (input in inputs) {
        val (action, numStr) = input.split(" ")
        val num = numStr.toInt()
        println("$action, $num")
        when (action) {
            "forward" -> {
                horizontalDistance += num
                depthDistance += aim * num
            }
            "down" -> {
                aim += num
            }
            "up" -> {
                aim -= num
            }
            else -> TODO()
        }
    }

    println("depthDistance: $depthDistance, horizontalDistance: $horizontalDistance, aim: $aim")

    println(depthDistance * horizontalDistance)
}

fun main() {
    val mainInput = readInput("day2")
    val testInput = readInput("day2_test")

//    part1(testInput)
//    part1(mainInput)

    part2(testInput)
    part2(mainInput)
}