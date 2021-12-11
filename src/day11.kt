import java.lang.IllegalStateException

fun main() {
    data class Point(
        val x: Int, val y: Int, var num: Int, var energyCharged: Int = 0, var
        alreadyFlashed: Boolean = false
    ) {
    }

    data class PuzzleInput(
        val data: List<List<Point>>
    ) {
        val height = data.size
        val width = data.first().size
        val garbagePoint = Point(-1, -1, 0, 0)

        fun print2dArray() {
            println(data.joinToString("\n") {
                it.joinToString {
                    it.num.toString()
                }
            })
        }

        fun getPoint(x: Int, y: Int): Point {
            if (x in 0 until width && y in 0 until height) {
                return data[y][x]
            }
            return garbagePoint
        }

        fun step(): Int {
            var numFlashesForStep = 0
            data.forEach {
                it.forEach { point ->
                    point.energyCharged++
                    point.alreadyFlashed = false
                }
            }

            while (data.any { it.any { pt -> pt.energyCharged > 0 } }) {
                data.forEach {
                    it.filter { pt -> pt.energyCharged > 0 }.forEach { pt ->
                        run {
                            if (pt.alreadyFlashed) {
                                pt.energyCharged = 0
                            } else {
                                pt.num += pt.energyCharged
                                pt.energyCharged = 0
                                if (pt.num > 9) {
                                    numFlashesForStep++
                                    pt.alreadyFlashed = true
                                    pt.num = 0
                                    val x = pt.x
                                    val y = pt.y
                                    getPoint(x, y - 1).energyCharged++
                                    getPoint(x, y + 1).energyCharged++

                                    getPoint(x - 1, y).energyCharged++
                                    getPoint(x + 1, y).energyCharged++

                                    getPoint(x - 1, y - 1).energyCharged++
                                    getPoint(x - 1, y + 1).energyCharged++

                                    getPoint(x + 1, y - 1).energyCharged++
                                    getPoint(x + 1, y + 1).energyCharged++
                                }
                            }
                        }
                    }
                }
            }

            print2dArray()

            if (numFlashesForStep == width * height) {
                throw IllegalStateException("All things flashing")
            }

            return numFlashesForStep
        }


    }


    fun part1(inputs: List<String>) {
        val data = inputs.mapIndexed { y, it ->
            it.toList().mapIndexed { x, char ->
                Point(x, y, char.digitToInt(), 0)
            }
        }
        val puzzleInput = PuzzleInput(data)

        puzzleInput.print2dArray()

        println()

        var totalFlashes = 0
        repeat(1000000) {
            println("step ${it + 1}")
            totalFlashes += puzzleInput.step()
        }
        println("total flashes: $totalFlashes")
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day11_test")
    val mainInput = readInput("day11")

    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}