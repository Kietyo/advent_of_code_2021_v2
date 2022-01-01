fun List<CharArray>.copy(): List<CharArray> {
    return this.map { it.clone() }
}

fun List<CharArray>.print() {
    println(this.joinToString("\n") { it.joinToString("") })
}

fun main() {
    fun createEmptyGrid(width: Int, height: Int): List<CharArray> {
        return (0 until height).map {
            CharArray(width) { '.' }
        }
    }

    fun step(grid: List<CharArray>): Pair<Int, List<CharArray>> {
        val height = grid.size
        val width = grid[0].size
        val newGrid = createEmptyGrid(width, height)

        var numCucumbersMoved = 0

        // East cucumbers move first
        for ((y, row) in grid.withIndex()) {
            for ((x, v) in row.withIndex()) {
                if (v == '>') {
                    val nextSpotX = (x + 1) % width
                    if (grid[y][nextSpotX] == '.') {
                        newGrid[y][nextSpotX] = '>'
                        numCucumbersMoved++
                    } else {
                        newGrid[y][x] = '>'
                    }
                }
            }
        }

        // South cucumbers move after
        for ((y, row) in grid.withIndex()) {
            for ((x, v) in row.withIndex()) {
                if (v == 'v') {
                    val nextSpotY = (y + 1) % height
                    if (newGrid[nextSpotY][x] == '.' && grid[nextSpotY][x] != 'v') {
                        newGrid[nextSpotY][x] = 'v'
                        numCucumbersMoved++
                    } else {
                        newGrid[y][x] = 'v'
                    }
                }
            }
        }
        return (numCucumbersMoved to newGrid)
    }

    fun part1(inputs: List<String>) {
        val grid = inputs.map {
            it.toCharArray()
        }

        println("Initial state:")
        grid.print()
        println()

        var step = grid
        var numSteps = 0
        while (true) {
            println("After ${numSteps + 1} steps:")
            val res = step(step)
            step = res.second
            step.print()
            println()
            if (res.first == 0) {
                println("No cucumbers moved!")
                break
            }
            numSteps++

        }
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day25_test")
    val mainInput = readInput("day25")

    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}