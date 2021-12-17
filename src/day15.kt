import java.util.*
import kotlin.Comparator


fun main() {
    data class Point(
        val x: Int, val y: Int
    )

    data class SearchState(
        val path: List<Point>,
        val riskCost: Int
    ) {
        fun getLast(): Point {
            return path.last()
        }

        fun getSecondToLastOrNull(): Point? {
            return path.getOrNull(path.size - 2)
        }
    }


    //    data class PuzzleInput(
    //        val data: List<List<Point>>
    //    )
    //    {
    //        val height = data.size
    //        val width = data.first().size
    //        val garbagePoint = Point(-1, -1, 0)
    //
    //        fun print2dArray() {
    //            println(data.joinToString("\n") {
    //                it.joinToString {
    //                    it.num.toString()
    //                }
    //            })
    //        }
    //
    //        fun getPoint(x: Int, y: Int): Point {
    //            if (x in 0 until width && y in 0 until height) {
    //                return data[y][x]
    //            }
    //            return garbagePoint
    //        }
    //
    //    }


    data class Grid(
        val grid: Array<IntArray>
    ) {
        val width = grid.first().size
        val height = grid.size

        fun getAllPoints(): List<Point> {
            return (0 until width).flatMap { x ->
                (0 until height).map { y ->
                    Point(x, y)
                }
            }
        }

        fun get(point: Point): Int {
            return grid[point.y][point.x]
        }

        fun getPointOrNull(x: Int, y: Int): Point? {
            if (x in 0 until width && y in 0 until height) {
                return Point(x, y)
            }
            return null
        }

        fun getNeighbors(point: Point): List<Point> {
            return listOfNotNull(
                getPointOrNull(point.x + 1, point.y),
                getPointOrNull(point.x, point.y + 1),
                getPointOrNull(point.x - 1, point.y),
                getPointOrNull(point.x, point.y - 1),
            )
        }

        fun print2dGrid() {
            println(
                """
                width: $width,
                height: $height
            """.trimIndent()
            )
            println(grid.joinToString("\n") { it.joinToString("") })

        }

    }


    fun dfs(searchState: SearchState, grid: Grid): SearchState {
        val stack = mutableListOf<SearchState>()
        stack.add(searchState)

        var result: SearchState? = null

        while (stack.isNotEmpty()) {
            val curr = stack.removeLast()
            val last = curr.getLast()

            if (last.x == grid.width - 1 && last.y == grid.height - 1) {
                result = curr
                break
            }

            val nextPoints = grid.getNeighbors(last).filter { !curr.path.contains(it) }
            for (nextPoint in nextPoints) {
                stack.add(SearchState(curr.path + nextPoint, curr.riskCost + grid.get(nextPoint)))
            }
        }

        return result!!
    }

    fun bfs(searchState: SearchState, grid: Grid, maxRiskCost: Int) {
        val queue = PriorityQueue<SearchState> { o1, o2 -> o1!!.riskCost - o2!!.riskCost }
        queue.add(searchState)

        var result: SearchState? = null
        val searchedStates = mutableSetOf<SearchState>()

        var explored = 0L
        var dfsRiskCost = maxRiskCost

        while (queue.isNotEmpty()) {
            explored++
            val curr = queue.remove()!!
            if (curr.riskCost >= dfsRiskCost) {
                // The current solution is already at or greater than the dfs solution
                continue
            }
            val last = curr.getLast()
            if (last.x == grid.width - 1 && last.y == grid.height - 1) {
                result = curr
                break
            }

            val nextPoints = grid.getNeighbors(last).filter { !curr.path.contains(it) }
            for (nextPoint in nextPoints) {
                queue.add(SearchState(curr.path + nextPoint, curr.riskCost + grid.get(nextPoint)))
            }
        }

        println(
            """
            result: $result
        """.trimIndent()
        )
    }

    fun dikjstras(grid: Grid) {
        val pointsToSearch = grid.getAllPoints().toMutableSet()
        val minDistanceToPoint = mutableMapOf<Point, Int>()
        pointsToSearch.forEach {
            minDistanceToPoint[it] = Int.MAX_VALUE
        }

        minDistanceToPoint[Point(0, 0)] = 0

        val prev = mutableMapOf<Point, Point>()

        while (pointsToSearch.isNotEmpty()) {
            println("Number of points remaining: ${pointsToSearch.size}")
            val minPoint = pointsToSearch.minOfWith(object : Comparator<Point> {
                override fun compare(o1: Point?, o2: Point?): Int {
                    return minDistanceToPoint[o1]!! - minDistanceToPoint[o2]!!
                }

            }) { it }

            val distanceToCurrent = minDistanceToPoint[minPoint]!!

            pointsToSearch.remove(minPoint)

            val nextPoints = grid.getNeighbors(minPoint)
            for (nextPoint in nextPoints) {
                val altDistance = distanceToCurrent + grid.get(nextPoint)
                if (altDistance < minDistanceToPoint[nextPoint]!!) {
                    minDistanceToPoint[nextPoint] = altDistance
                    prev[nextPoint] = minPoint
                }
            }
        }

        println(
            """
            minDistanceToPoint: $minDistanceToPoint
        """.trimIndent()
        )
        println(prev.entries.joinToString("\n"))

        println(minDistanceToPoint[Point(grid.width - 1, grid.height - 1)])
    }

    fun dikjstras_v2(grid: Grid) {
        val minDistanceToPoint = mutableMapOf<Point, Int>()
        grid.getAllPoints().forEach {
            minDistanceToPoint[it] = Int.MAX_VALUE
        }

        minDistanceToPoint[Point(0, 0)] = 0

        val pointsToSearch = mutableSetOf<Point>()
        pointsToSearch.add(Point(0, 0))

        val alreadySearched = mutableSetOf<Point>()


        var explored = 0
        while (pointsToSearch.isNotEmpty()) {
            explored++
            println("Number of points remaining: ${pointsToSearch.size}")
            if (explored == 2) {
                println("here")
            }
            val minPoint = pointsToSearch.minByOrNull { minDistanceToPoint[it]!! }!!
            val distanceToCurrent = minDistanceToPoint[minPoint]!!
            pointsToSearch.remove(minPoint)
            if (alreadySearched.contains(minPoint)) {
                continue
            }

            val nextPoints = grid.getNeighbors(minPoint).filter {
                minDistanceToPoint[it] == Int.MAX_VALUE
            }
            for (nextPoint in nextPoints) {
                val altDistance = distanceToCurrent + grid.get(nextPoint)
                if (altDistance < minDistanceToPoint[nextPoint]!!) {
                    minDistanceToPoint[nextPoint] = altDistance
                    pointsToSearch.add(nextPoint)
                }
            }
        }

        println(
            """
            minDistanceToPoint: $minDistanceToPoint
        """.trimIndent()
        )
        //        println(prev.entries.joinToString("\n"))

        println(minDistanceToPoint[Point(grid.width - 1, grid.height - 1)])
    }

    fun part1(inputs: List<String>) {
        val grid = Grid(inputs.map { row -> row.toList().map { it.digitToInt() }.toIntArray() }
            .toTypedArray())

        val initialState = SearchState(listOf(Point(0, 0)), 0)

        //        val dfsSolution = dfs(initialState, grid)
        //
        //        println(dfsSolution)

        //        bfs(initialState, grid, dfsSolution.riskCost)

        dikjstras(grid)
    }

    fun part2(inputs: List<String>) {
        val height = inputs.size
        val width = inputs.first().length

        val originalData = inputs.map { row -> row.toList().map { it.digitToInt() }.toIntArray() }

        val actualHeight = height * 5
        val actualWidth = width * 5

        val data = Array(actualHeight) { IntArray(actualWidth) }
        for (yOffset in 0 until 5) {
            for (xOffset in 0 until 5) {
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        data[y + yOffset * height][x + xOffset * width] =
                            (originalData[y][x] + xOffset + yOffset - 1) % 9 + 1
                    }
                }
            }
        }


        val grid = Grid(data)

        grid.print2dGrid()
        //
        //        val initialState = SearchState(listOf(Point(0, 0)), 0)
        //
        //        //        val dfsSolution = dfs(initialState, grid)
        //        //
        //        //        println(dfsSolution)
        //
        //        //        bfs(initialState, grid, dfsSolution.riskCost)
        //
        dikjstras_v2(grid)
    }

    val testInput = readInput("day15_test")
    val mainInput = readInput("day15")

    //        part1(testInput)
    //    part1(mainInput)
    //
    //        part2(testInput)
    part2(mainInput)
}