fun main() {
    fun printGrid(grid: Array<BooleanArray>) {
        println(grid.joinToString("\n") { it.map { b -> if (b) '#' else '.' }.joinToString("") })
    }

    fun foldAlongY(grid: Array<BooleanArray>, y: Int): Array<BooleanArray> {
        println("Folding along y: $y")
        val height = grid.size
        val width = grid[0].size

        require(height / 2 == y)

        val newHeight = height / 2

        val newGrid = Array<BooleanArray>(newHeight) { BooleanArray(width) }

        for (y in 0 until newHeight) {
            for (x in 0 until width) {
                newGrid[y][x] = grid[y][x] || grid[height - 1 - y][x]
            }
        }

        println(
            """
            height / 2: ${height / 2}
            newHeight: $newHeight
        """.trimIndent()
        )

        println("newGrid")
        printGrid(newGrid)
        return newGrid
    }

    fun foldAlongX(grid: Array<BooleanArray>, x: Int): Array<BooleanArray> {
        println("Folding along x: $x")
        val height = grid.size
        val width = grid[0].size

        require(width / 2 == x)

        val newWidth = width / 2

        val newGrid = Array<BooleanArray>(height) { BooleanArray(newWidth) }

        for (y in 0 until height) {
            for (x in 0 until newWidth) {
                newGrid[y][x] = grid[y][x] || grid[y][width - 1 - x]
            }
        }

        println(
            """
            newWidth: $newWidth
        """.trimIndent()
        )

        println("newGrid")
        printGrid(newGrid)
        return newGrid
    }

    fun part1(inputs: List<String>) {
        val points = inputs.filter { it.contains(",") }.map {
            val split = it.split(",")
            Pair<Int, Int>(split.first().toInt(), split.last().toInt())
        }
        val instructionInputs = inputs.filter { it.contains("fold along") }

        println(points)

        val maxX = points.maxOf { it.first } + 1
        val maxY = points.maxOf { it.second } + 1

        val grid = Array<BooleanArray>(maxY) { BooleanArray(maxX) }

        points.forEach { grid[it.second][it.first] = true }

        println(
            """
            inputs: $inputs
            pointInputs: $points
            instructionInputs: $instructionInputs
            maxX: $maxX
            maxY: $maxY
        """.trimIndent()
        )

        println("grid:")
        printGrid(grid)

        var currentGrid = grid
        instructionInputs.forEach {
            val splitByY = it.contains("fold along y")
            val placeToSplit = it.split("=").last().toInt()


            currentGrid = if (splitByY) foldAlongY(currentGrid, placeToSplit)
            else foldAlongX(currentGrid, placeToSplit)

            val numDots = currentGrid.sumOf { it.sumOf { b -> if (b) 1L else 0L } }


            println(
                """
                placeToSplit: $placeToSplit
                numDots: $numDots
            """.trimIndent()
            )

        }


        //        foldAlongX(foldAlongY(grid, 7), 5)
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day13_test")
    val mainInput = readInput("day13")

    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}